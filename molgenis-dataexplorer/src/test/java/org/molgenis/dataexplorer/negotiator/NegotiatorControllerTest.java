package org.molgenis.dataexplorer.negotiator;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMetadata.BIOBANK_ID;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMetadata.COLLECTION_ID;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMetadata.ENABLED_EXPRESSION;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMetadata;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.util.i18n.AllPropertiesMessageSource;
import org.molgenis.web.rsql.QueryRsql;
import org.molgenis.web.rsql.QueryRsqlConverter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

@WebAppConfiguration
class NegotiatorControllerTest {
  private NegotiatorController negotiatorController;

  @Mock private RestTemplate restTemplate;
  @Mock private UserPermissionEvaluator permissionService;
  @Mock private DataService dataService;
  @Mock private QueryRsqlConverter rsqlQueryConverter;
  @Mock private Repository<Entity> repo;
  @Mock private NegotiatorEntityConfig negotiatorEntityConfig;
  @Mock private NegotiatorConfig negotiatorConfig;
  @Mock private QueryRsql queryRsql;
  @Mock private Attribute collectionAttr;
  @Mock private Attribute biobackAttr;
  @Mock private Query<Entity> molgenisQuery;
  @Mock private JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

  @Captor private ArgumentCaptor<HttpEntity<NegotiatorQuery>> queryCaptor;

  @BeforeEach
  void beforeMethod() {
    initMocks(this);

    /* Negotiator config mock */
    Query<NegotiatorEntityConfig> query =
        new QueryImpl<NegotiatorEntityConfig>()
            .eq(NegotiatorEntityConfigMetadata.ENTITY, "molgenis_id_1");
    when(dataService.findOne(
            NegotiatorEntityConfigMetadata.NEGOTIATORENTITYCONFIG,
            query,
            NegotiatorEntityConfig.class))
        .thenReturn(negotiatorEntityConfig);

    when(collectionAttr.getName()).thenReturn("collectionAttr");
    when(collectionAttr.getDataType()).thenReturn(AttributeType.STRING);
    doReturn(collectionAttr).when(negotiatorEntityConfig).getEntity(COLLECTION_ID, Attribute.class);

    when(biobackAttr.getName()).thenReturn("biobackAttr");
    when(biobackAttr.getDataType()).thenReturn(AttributeType.STRING);
    doReturn(biobackAttr).when(negotiatorEntityConfig).getEntity(BIOBANK_ID, Attribute.class);

    when(negotiatorEntityConfig.getString(ENABLED_EXPRESSION)).thenReturn("$(enabled).value()");
    when(negotiatorEntityConfig.getNegotiatorConfig()).thenReturn(negotiatorConfig);

    /* get EntityCollection mock */
    when(dataService.getRepository("molgenis_id_1")).thenReturn(repo);
    when(queryRsql.createQuery(repo)).thenReturn(molgenisQuery);
    when(rsqlQueryConverter.convert("*=q=MOLGENIS")).thenReturn(queryRsql);

    LocaleContextHolder.setLocale(Locale.ENGLISH);
    AllPropertiesMessageSource messageSource = new AllPropertiesMessageSource();
    messageSource.addMolgenisNamespaces("dataexplorer");

    negotiatorController =
        new NegotiatorController(
            restTemplate,
            permissionService,
            dataService,
            rsqlQueryConverter,
            jsMagmaScriptEvaluator,
            messageSource);
  }

  @Test
  void testValidateNegotiatorExport() {
    NegotiatorRequest request =
        NegotiatorRequest.create(
            "http://molgenis.org",
            "molgenis_id_1",
            "*=q=MOLGENIS",
            "a nice molgenis query",
            "Sjfg03Msmdp92Md82103FNskas9H735F");

    Entity entity = mock(Entity.class);
    String entityOneLabel = "Entity One";
    when(entity.getLabelValue()).thenReturn(entityOneLabel);
    when(entity.get("enabled")).thenReturn(true);

    when(molgenisQuery.findAll()).thenReturn(Stream.of(entity));
    when(jsMagmaScriptEvaluator.eval("$(enabled).value()", entity)).thenReturn(TRUE);

    ExportValidationResponse actual = negotiatorController.validateNegotiatorExport(request);
    List<String> enabledCollections = Collections.singletonList(entityOneLabel);
    ExportValidationResponse expected =
        ExportValidationResponse.create(true, "", enabledCollections, emptyList());

    assertEquals(expected, actual);
  }

  @Test
  void testValidateNegotiatorExportNoConfig() {
    NegotiatorRequest request =
        NegotiatorRequest.create(
            "http://molgenis.org",
            "molgenis_id_1",
            "*=q=MOLGENIS",
            "a nice molgenis query",
            "Sjfg03Msmdp92Md82103FNskas9H735F");

    Query<NegotiatorEntityConfig> query =
        new QueryImpl<NegotiatorEntityConfig>()
            .eq(NegotiatorEntityConfigMetadata.ENTITY, "molgenis_id_1");

    when(dataService.findOne(
            NegotiatorEntityConfigMetadata.NEGOTIATORENTITYCONFIG,
            query,
            NegotiatorEntityConfig.class))
        .thenReturn(null);

    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> negotiatorController.validateNegotiatorExport(request));
    assertThat(exception.getMessage())
        .containsPattern("No negotiator configuration found for the selected entity");
  }

  @Test
  void testValidateNegotiatorExportEmptyCollections() {
    NegotiatorRequest request =
        NegotiatorRequest.create(
            "http://molgenis.org",
            "molgenis_id_1",
            "*=q=MOLGENIS",
            "a nice molgenis query",
            "Sjfg03Msmdp92Md82103FNskas9H735F");

    Entity entity = mock(Entity.class);
    when(entity.getLabelValue()).thenReturn("Entity One");
    when(entity.get("enabled")).thenReturn(true);

    when(molgenisQuery.findAll()).thenReturn(Stream.empty());

    ExportValidationResponse actual = negotiatorController.validateNegotiatorExport(request);
    ExportValidationResponse expected =
        ExportValidationResponse.create(
            false,
            "Please make sure your selection contains at least 1 row that supports the negotiator.");

    assertEquals(expected, actual);
  }

  @Test
  void testValidateNegotiatorExportContainsDisabledCollections() {
    NegotiatorRequest request =
        NegotiatorRequest.create(
            "http://molgenis.org",
            "molgenis_id_1",
            "*=q=MOLGENIS",
            "a nice molgenis query",
            "Sjfg03Msmdp92Md82103FNskas9H735F");

    Entity entityEnabled = mock(Entity.class);
    String entityOneLabel = "Entity One";
    String entityDisabledLabel = "Entity Disabled";
    when(entityEnabled.getLabelValue()).thenReturn(entityOneLabel);
    when(entityEnabled.get("enabled")).thenReturn(true);

    Entity entityDisabled = mock(Entity.class);
    when(entityDisabled.getLabelValue()).thenReturn(entityDisabledLabel);
    when(entityDisabled.get("enabled")).thenReturn(false);

    when(molgenisQuery.findAll()).thenReturn(Stream.of(entityEnabled, entityDisabled));

    when(jsMagmaScriptEvaluator.eval("$(enabled).value()", entityEnabled)).thenReturn(TRUE);
    when(jsMagmaScriptEvaluator.eval("$(enabled).value()", entityDisabled)).thenReturn(FALSE);

    ExportValidationResponse actual = negotiatorController.validateNegotiatorExport(request);
    List<String> enabledCollections = Collections.singletonList(entityOneLabel);
    List<String> disabledCollections = Collections.singletonList(entityDisabledLabel);
    ExportValidationResponse expected =
        ExportValidationResponse.create(
            true,
            "1 of 2 collections do not support this functionality. Do you want to continue?",
            enabledCollections,
            disabledCollections);

    assertEquals(expected, actual);
  }

  @Test
  void testValidateNegotiatorExportAllCollectionsAreDisabled() {
    NegotiatorRequest request =
        NegotiatorRequest.create(
            "http://molgenis.org",
            "molgenis_id_1",
            "*=q=MOLGENIS",
            "a nice molgenis query",
            "Sjfg03Msmdp92Md82103FNskas9H735F");

    Entity entityDisabled = mock(Entity.class);
    String entityDisabledLabel = "Entity Disabled";
    when(entityDisabled.getLabelValue()).thenReturn(entityDisabledLabel);
    when(entityDisabled.get("enabled")).thenReturn(false);

    when(molgenisQuery.findAll()).thenReturn(Stream.of(entityDisabled));

    when(jsMagmaScriptEvaluator.eval("$(enabled).value()", entityDisabled)).thenReturn(FALSE);

    ExportValidationResponse actual = negotiatorController.validateNegotiatorExport(request);
    List<String> disabledCollections = Collections.singletonList(entityDisabledLabel);
    ExportValidationResponse expected =
        ExportValidationResponse.create(
            false,
            "Please make sure your selection contains at least 1 row that supports the negotiator.",
            emptyList(),
            disabledCollections);

    assertEquals(expected, actual);
  }

  @Test
  void testExportToNegotiator() {
    NegotiatorRequest request =
        NegotiatorRequest.create(
            "http://molgenis.org",
            "molgenis_id_1",
            "*=q=MOLGENIS",
            "a nice molgenis query",
            "Sjfg03Msmdp92Md82103FNskas9H735F");

    when(negotiatorConfig.getUsername()).thenReturn("username");
    when(negotiatorConfig.getPassword()).thenReturn("password");
    when(negotiatorConfig.getNegotiatorURL()).thenReturn("http://directory.com");

    when(restTemplate.postForLocation(eq("http://directory.com"), queryCaptor.capture()))
        .thenReturn(URI.create("http://directory.com/request/1280"));

    String actual = negotiatorController.exportToNegotiator(request);
    String expected = "http://directory.com/request/1280";

    assertEquals(expected, actual);
  }

  @Test
  void testExportToNegotiatorMissingNegotiatorURL() {
    NegotiatorRequest request =
        NegotiatorRequest.create(
            "http://molgenis.org",
            "molgenis_id_1",
            "*=q=MOLGENIS",
            "a nice molgenis query",
            "Sjfg03Msmdp92Md82103FNskas9H735F");

    when(negotiatorConfig.getUsername()).thenReturn("username");
    when(negotiatorConfig.getPassword()).thenReturn("password");

    when(restTemplate.postForLocation(eq("http://directory.com"), queryCaptor.capture()))
        .thenReturn(URI.create("http://directory.com/request/1280"));

    Exception exception =
        assertThrows(
            IllegalStateException.class, () -> negotiatorController.exportToNegotiator(request));
    assertThat(exception.getMessage()).containsPattern("Negotiator config URL can't be null");
  }

  @Test
  void testShowButtonNoPermissionsOnPlugin() {
    when(permissionService.hasPermission(
            new PluginIdentity("directory"), PluginPermission.VIEW_PLUGIN))
        .thenReturn(false);
    assertFalse(negotiatorController.showDirectoryButton("molgenis_id_1"));
  }

  @Test
  void testShowButton() {
    when(permissionService.hasPermission(
            new PluginIdentity("directory"), PluginPermission.VIEW_PLUGIN))
        .thenReturn(true);
    assertTrue(negotiatorController.showDirectoryButton("molgenis_id_1"));
  }

  @Test
  void testShowButtonPermissionsOnPluginNoConfig() {
    when(permissionService.hasPermission(
            new PluginIdentity("directory"), PluginPermission.VIEW_PLUGIN))
        .thenReturn(false);

    assertFalse(negotiatorController.showDirectoryButton("blah2"));
  }
}
