package org.molgenis.data.rest.v3;

import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.rest.v3.model.*;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.rest.v3.RestControllerV3.URI;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Controller
@RequestMapping(URI)
public class RestControllerV3
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerV3.class);

	public static final String URI = "/api/v3";

	private final RestService restService;

	@Autowired
	public RestControllerV3(RestService restService)
	{
		this.restService = requireNonNull(restService);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = POST)
	@ResponseStatus(CREATED)
	public void createEntity(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody CreateEntityRequest createEntityRequest, HttpServletResponse httpServletResponse)
	{
		CreateEntityResponse createResponse = restService.createEntity(entityTypeId, createEntityRequest);
		httpServletResponse.setHeader(LOCATION, createResponse.getLocation().toString());
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = POST, headers = "X-Action=bulk")
	@ResponseBody
	public CreateEntitiesResponse createEntities(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody CreateEntitiesRequest createEntitiesRequest)
	{
		return restService.createEntities(entityTypeId, createEntitiesRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}/{entityId:.+}", method = GET)
	@ResponseBody
	public ReadEntityResponse readEntity(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("entityId") String entityId, @Valid ReadEntityRequest readEntityRequest)
	{
		return restService.readEntity(entityTypeId, entityId, readEntityRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}/{entityId:.+}", method = POST, headers = "X-HTTP-Method-Override=GET")
	@ResponseBody
	public ReadEntityResponse readEntityOverride(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("entityId") String entityId, @Valid ReadEntityRequest readEntityRequest)
	{
		return restService.readEntity(entityTypeId, entityId, readEntityRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = GET)
	@ResponseBody
	public ReadEntitiesResponse readEntities(@PathVariable("entityTypeId") String entityTypeId,
			@Valid ReadEntitiesRequest readEntitiesRequest)
	{
		return restService.readEntities(entityTypeId, readEntitiesRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = POST, headers = "X-HTTP-Method-Override=GET")
	@ResponseBody
	public ReadEntitiesResponse readEntitiesOverride(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody ReadEntitiesRequest readEntitiesRequest)
	{
		return restService.readEntities(entityTypeId, readEntitiesRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}/{entityId:.+}", method = PUT)
	@ResponseStatus(NO_CONTENT)
	public void updateEntity(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("entityId") String entityId, @Valid @RequestBody UpdateEntityRequest updateEntityRequest)
	{
		restService.updateEntity(entityTypeId, entityId, updateEntityRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}/{entityId:.+}", method = POST, headers = "X-HTTP-Method-Override=PUT")
	@ResponseStatus(NO_CONTENT)
	public void updateEntityOverride(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("entityId") String entityId, @Valid @RequestBody UpdateEntityRequest updateEntityRequest)
	{
		restService.updateEntity(entityTypeId, entityId, updateEntityRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = PUT, headers = "X-Action=bulk")
	@ResponseStatus(NO_CONTENT)
	public void updateEntities(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody UpdateEntitiesRequest updateEntitiesRequest)
	{
		restService.updateEntities(entityTypeId, updateEntitiesRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = POST, headers = { "X-Action=bulk",
			"X-HTTP-Method-Override=PUT" })
	@ResponseStatus(NO_CONTENT)
	public void updateEntitiesOverride(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody UpdateEntitiesRequest updateEntitiesRequest)
	{
		restService.updateEntities(entityTypeId, updateEntitiesRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}/{entityId:.+}", method = PATCH)
	@ResponseStatus(NO_CONTENT)
	public void partialUpdateEntity(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("entityId") String entityId,
			@Valid @RequestBody PartialUpdateEntityRequest partialUpdateEntityRequest)
	{
		restService.partialUpdateEntity(entityTypeId, entityId, partialUpdateEntityRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}/{entityId:.+}", method = POST, headers = "X-HTTP-Method-Override=PATCH")
	@ResponseStatus(NO_CONTENT)
	public void partialUpdateEntityOverride(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("entityId") String entityId,
			@Valid @RequestBody PartialUpdateEntityRequest partialUpdateEntityRequest)
	{
		restService.partialUpdateEntity(entityTypeId, entityId, partialUpdateEntityRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = PATCH, headers = "X-Action=bulk")
	@ResponseStatus(NO_CONTENT)
	public void partialUpdateEntities(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody PartialUpdateEntitiesRequest partialUpdateEntitiesRequest)
	{
		restService.partialUpdateEntities(entityTypeId, partialUpdateEntitiesRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = POST, headers = { "X-Action=bulk",
			"X-HTTP-Method-Override=PATCH" })
	@ResponseStatus(NO_CONTENT)
	public void partialUpdateEntitiesOverride(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody PartialUpdateEntitiesRequest partialUpdateEntitiesRequest)
	{
		restService.partialUpdateEntities(entityTypeId, partialUpdateEntitiesRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}/{entityId:.+}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void deleteEntity(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("entityId") String entityId)
	{
		restService.deleteEntity(entityTypeId, entityId);
	}

	@RequestMapping(value = "/{entityTypeId:.+}/{entityId:.+}", method = POST, headers = "X-HTTP-Method-Override=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deleteEntityOverride(@PathVariable("entityTypeId") String entityTypeId,
			@PathVariable("entityId") String entityId)
	{
		restService.deleteEntity(entityTypeId, entityId);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = DELETE, headers = "X-Action=bulk")
	@ResponseStatus(NO_CONTENT)
	public void deleteEntities(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody DeleteEntitiesRequest deleteEntitiesRequest)
	{
		restService.deleteEntities(entityTypeId, deleteEntitiesRequest);
	}

	@RequestMapping(value = "/{entityTypeId:.+}", method = DELETE, headers = { "X-Action=bulk",
			"X-HTTP-Method-Override=DELETE" })
	@ResponseStatus(NO_CONTENT)
	public void deleteEntitiesOverride(@PathVariable("entityTypeId") String entityTypeId,
			@Valid @RequestBody DeleteEntitiesRequest deleteEntitiesRequest)
	{
		restService.deleteEntities(entityTypeId, deleteEntitiesRequest);
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		LOG.info("Operation failed.", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisDataAccessException.class)
	@ResponseStatus(FORBIDDEN)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataAccessException(MolgenisDataAccessException e)
	{
		LOG.debug("Data access exception occurred.", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleBindException(BindException e)
	{
		return handleBindException(e, e.getBindingResult());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
	{
		return handleBindException(e, e.getBindingResult());
	}

	private ErrorMessageResponse handleBindException(Exception e, BindingResult bindingResult)
	{
		LOG.info("Invalid method arguments.", e);
		Stream<String> errorMessages = bindingResult.getFieldErrors().stream().map(FieldError::getDefaultMessage);
		return new ErrorMessageResponse(errorMessages.map(ErrorMessage::new).collect(toList()));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("Runtime exception occurred.", e);
		return new ErrorMessageResponse(new ErrorMessage("Server error occured")); // do not expose message
	}
}
