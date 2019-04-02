package org.molgenis.api.permissions;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.permissions.PermissionResponseUtils.getPermissionResponse;
import static org.molgenis.api.permissions.PermissionsApiController.BASE_URI; // NOSONAR
import static org.molgenis.api.permissions.SidConversionUtils.getSid;
import static org.molgenis.api.permissions.SidConversionUtils.getSids;

import com.google.common.collect.Lists;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.api.ApiController;
import org.molgenis.api.model.response.PagedApiResponse;
import org.molgenis.api.permissions.exceptions.PageWithoutPageSizeException;
import org.molgenis.api.permissions.exceptions.UnsupportedPermissionQueryException;
import org.molgenis.api.permissions.model.request.DeletePermissionRequest;
import org.molgenis.api.permissions.model.request.SetObjectPermissionRequest;
import org.molgenis.api.permissions.model.request.SetTypePermissionsRequest;
import org.molgenis.api.permissions.model.response.GetObjectPermissionResponse;
import org.molgenis.api.permissions.model.response.GetPermissionsResponse;
import org.molgenis.api.permissions.model.response.GetTypePermissionsResponse;
import org.molgenis.api.permissions.model.response.ObjectPermissionsResponse;
import org.molgenis.api.permissions.model.response.PermissionResponse;
import org.molgenis.api.permissions.model.response.TypePermissionsResponse;
import org.molgenis.api.permissions.rsql.PermissionRsqlVisitor;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.molgenis.security.acl.ObjectIdentityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api("Permissions API")
@RestController
@RequestMapping(BASE_URI)
@Transactional
public class PermissionsApiController extends ApiController {

  private static final String PERMISSION_API_IDENTIFIER = "permissions";
  private static final int VERSION = 1;
  public static final String BASE_URI = "/api/" + PERMISSION_API_IDENTIFIER + "/v" + VERSION;
  static final Integer DEFAULT_PAGE = 1;
  static final Integer DEFAULT_PAGESIZE = 10000;

  private static final String TYPE = "type";
  public static final String TYPES = TYPE + "s";
  private static final String TYPE_ID = TYPE + "Id";

  private static final String OBJECT = "object";
  public static final String OBJECTS = OBJECT + "s";
  private static final String OBJECT_ID = OBJECT + "Id";

  private final PermissionApiService permissionApiService;
  private final RSQLParser rsqlParser;
  private final ObjectIdentityService objectIdentityService;

  public PermissionsApiController(
      PermissionApiService permissionApiService,
      RSQLParser rsqlParser,
      ObjectIdentityService objectIdentityService) {
    super(PERMISSION_API_IDENTIFIER, VERSION);
    this.permissionApiService = requireNonNull(permissionApiService);
    this.rsqlParser = requireNonNull(rsqlParser);
    this.objectIdentityService = requireNonNull(objectIdentityService);
  }

  @PostMapping(value = TYPES + "/{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Create a ACL class, typically this is done to row level secure an entity",
      response = ResponseEntity.class)
  public ResponseEntity enableRLS(
      HttpServletRequest request, @PathVariable(value = TYPE_ID) String typeId)
      throws URISyntaxException {
    permissionApiService.addClass(typeId);
    return ResponseEntity.created(new URI(request.getRequestURI())).build();
  }

  @GetMapping(value = TYPES)
  @ApiOperation(value = "Get a list of acl classes in the system", response = ResponseEntity.class)
  public List<String> getRlsEntities() {
    return permissionApiService.getClasses();
  }

  @GetMapping(value = TYPES + "/permissions/{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Get a list of permissions that can be used on a acl class",
      response = List.class)
  public Set<String> getSuitablePermissions(@PathVariable(value = TYPE_ID) String typeId) {
    return permissionApiService.getSuitablePermissionsForType(typeId);
  }

  @PostMapping(value = OBJECTS + "/{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(value = "Create an acl for a entity", response = ResponseEntity.class)
  public ResponseEntity createClass(
      HttpServletRequest request,
      @PathVariable(TYPE_ID) String typeId,
      @PathVariable(OBJECT_ID) String identifier)
      throws URISyntaxException {
    permissionApiService.createAcl(typeId, identifier);
    return ResponseEntity.created(new URI(request.getRequestURI())).build();
  }

  @GetMapping(value = OBJECTS + "/{" + TYPE_ID + "}")
  @ApiOperation(
      value =
          "Get a list ace's for a entity. Typically this is a row in a row level secured entity.",
      response = List.class)
  public PagedApiResponse getAcls(
      @PathVariable(value = TYPE_ID) String typeId,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
    validateQueryParams(page, pageSize, false);
    if (page == null) {
      page = DEFAULT_PAGE;
      pageSize = DEFAULT_PAGESIZE;
    }

    List<String> data = permissionApiService.getAcls(typeId, page, pageSize);

    int totalItems = objectIdentityService.getNrOfObjectIdentities(typeId);
    return getPermissionResponse("", page, pageSize, totalItems, data);
  }

  @GetMapping(value = "{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(value = "Gets permissions on a single acl", response = ResponseEntity.class)
  public GetObjectPermissionResponse getPermissionsForObject(
      @PathVariable(TYPE_ID) String typeId,
      @PathVariable(OBJECT_ID) String identifier,
      @RequestParam(value = "q", required = false) String queryString,
      @RequestParam(value = "inheritance", defaultValue = "false", required = false)
          boolean inheritance) {
    Set<Sid> sids = getSidsFromQuery(queryString);
    List<PermissionResponse> permissionResponses =
        permissionApiService.getPermission(typeId, identifier, sids, inheritance);
    return GetObjectPermissionResponse.create(permissionResponses);
  }

  @GetMapping(value = "{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Gets all permissions for all acls of a certain class",
      response = ResponseEntity.class)
  public PagedApiResponse getPermissionsForClass(
      @PathVariable(value = TYPE_ID) String typeId,
      @RequestParam(value = "q", required = false) String queryString,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "inheritance", defaultValue = "false", required = false)
          boolean inheritance) {
    validateQueryParams(page, pageSize, inheritance);
    Set<Sid> sids = getSidsFromQuery(queryString);

    ArrayList<ObjectPermissionsResponse> permissions;
    PagedApiResponse response;
    if (page != null) {
      permissions =
          Lists.newArrayList(
              permissionApiService.getPagedPermissionsForType(typeId, sids, page, pageSize));
      Integer totalItems = objectIdentityService.getNrOfObjectIdentities(typeId, sids);
      response =
          getPermissionResponse(
              queryString,
              page,
              pageSize,
              totalItems,
              GetTypePermissionsResponse.create(permissions));
    } else {
      permissions =
          Lists.newArrayList(permissionApiService.getPermissionsForType(typeId, sids, inheritance));
      response = getPermissionResponse(queryString, GetTypePermissionsResponse.create(permissions));
    }
    return response;
  }

  @GetMapping()
  @ApiOperation(
      value = "Gets all permissions for one or more users or roles",
      response = ResponseEntity.class)
  public GetPermissionsResponse getPermissionsForUser(
      @RequestParam(value = "q", required = false) String queryString,
      @RequestParam(value = "inheritance", defaultValue = "false", required = false)
          boolean inheritance) {
    Set<Sid> sids = getSidsFromQuery(queryString);
    List<TypePermissionsResponse> permissions =
        permissionApiService.getAllPermissions(sids, inheritance);
    return GetPermissionsResponse.create(permissions);
  }

  @PatchMapping(value = "{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(
      value = "Update a permission on a single acl for one or more users or roles",
      response = ResponseEntity.class)
  public ResponseEntity setPermission(
      @PathVariable(value = TYPE_ID) String typeId,
      @PathVariable(value = OBJECT_ID) String identifier,
      @RequestBody SetObjectPermissionRequest request) {
    permissionApiService.updatePermission(request.getPermissions(), typeId, identifier);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping(value = "{" + TYPE_ID + "}")
  @ApiOperation(
      value = "Update a list of permissions on acls belonging to a certain acl class",
      response = ResponseEntity.class)
  public ResponseEntity setClassPermissions(
      @PathVariable(value = TYPE_ID) String typeId,
      @RequestBody SetTypePermissionsRequest request) {
    permissionApiService.updatePermissions(request.getObjects(), typeId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "{" + TYPE_ID + "}")
  @ApiOperation(value = "Create a list of permissions on an acl for a single user or role")
  public ResponseEntity<Object> createPermissions(
      HttpServletRequest request,
      @PathVariable(value = TYPE_ID) String typeId,
      @RequestBody SetTypePermissionsRequest setTypePermissionsRequest)
      throws URISyntaxException {
    permissionApiService.createPermissions(setTypePermissionsRequest.getObjects(), typeId);
    return ResponseEntity.created(new URI(request.getRequestURI())).build();
  }

  @PostMapping(value = "{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(value = "Create a permission on acls for a single user or role")
  public ResponseEntity<Object> createPermission(
      HttpServletRequest request,
      @PathVariable(value = TYPE_ID) String typeId,
      @PathVariable(value = OBJECT_ID) String identifier,
      @RequestBody SetObjectPermissionRequest setIdentityPermissionRequest)
      throws URISyntaxException {
    permissionApiService.createPermission(
        setIdentityPermissionRequest.getPermissions(), typeId, identifier);
    return ResponseEntity.created(new URI(request.getRequestURI())).build();
  }

  @DeleteMapping(value = "{" + TYPE_ID + "}/{" + OBJECT_ID + "}")
  @ApiOperation(
      value = "Delete a permission on an acl for a single user or role",
      response = ResponseEntity.class)
  public ResponseEntity deletePermission(
      @PathVariable(value = TYPE_ID) String typeId,
      @PathVariable(value = OBJECT_ID) String identifier,
      @RequestBody DeletePermissionRequest request) {
    Sid sid = getSid(request.getUser(), request.getRole());
    permissionApiService.deletePermission(sid, typeId, identifier);
    return ResponseEntity.noContent().build();
  }

  private Set<Sid> getSidsFromQuery(String queryString) {
    Set<Sid> sids = Collections.emptySet();
    if (StringUtils.isNotEmpty(queryString)) {
      Node node = rsqlParser.parse(queryString);
      PermissionsQuery permissionsQuery = node.accept(new PermissionRsqlVisitor());
      sids = new LinkedHashSet<>(getSids(permissionsQuery));
    }
    return sids;
  }

  private void validateQueryParams(Integer page, Integer pageSize, boolean inheritance) {
    if ((page == null && pageSize != null) || (page != null && pageSize == null)) {
      throw new PageWithoutPageSizeException();
    }
    if (page != null && inheritance) {
      throw new UnsupportedPermissionQueryException();
    }
  }
}