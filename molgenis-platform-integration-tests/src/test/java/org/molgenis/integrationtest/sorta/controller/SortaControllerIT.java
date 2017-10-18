package org.molgenis.integrationtest.sorta.controller;

import com.google.common.io.Resources;
import org.molgenis.DatabaseConfig;
import org.molgenis.auth.TokenMetaData;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.integrationtest.data.DataTestConfig;
import org.molgenis.integrationtest.data.settings.SettingsTestConfig;
import org.molgenis.integrationtest.file.FileTestConfig;
import org.molgenis.integrationtest.js.JsTestConfig;
import org.molgenis.integrationtest.ontology.sorta.SortaTestConfig;
import org.molgenis.integrationtest.script.ScriptConfig;
import org.molgenis.integrationtest.ui.UiTestConfig;
import org.molgenis.integrationtest.util.UtilTestConfig;
import org.molgenis.integrationtest.utils.AbstractMolgenisIntegrationTests;
import org.molgenis.integrationtest.utils.config.SecurityITConfig;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.sorta.controller.SortaController;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.molgenis.ontology.sorta.job.SortaJobExecutionFactory;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.integrationtest.utils.config.SecurityITConfig.SUPERUSER_NAME;
import static org.molgenis.ontology.sorta.controller.SortaController.*;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData.INPUT_TERM;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.token.TokenExtractor.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = { SortaControllerIT.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class SortaControllerIT extends AbstractMolgenisIntegrationTests
{

	@Autowired
	private SortaJobExecutionFactory sortaJobExecutionFactory;
	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private MatchingTaskContentMetaData matchingTaskContentMetaData;
	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private DataService dataService;
	@Autowired
	private UserFactory userFactory;

	@Autowired
	private AppSettings appSettings;

	public void beforeITMethod()
	{
		User user = userFactory.create();
		user.setUsername(SecurityITConfig.SUPERUSER_NAME);
		user.setPassword(SecurityITConfig.SUPERUSER_NAME);
		user.setEmail("admin@molgenis.org");
		dataService.add(UserMetaData.USER, user);
		appSettings.setMenu(
				"{\"type\":\"menu\",\"id\":\"main\",\"label\":\"Home\",\"items\":[{\"type\":\"plugin\",\"id\":\"sortaservice\",\"label\":\"SORTA\",\"params\":\"\"},{\"type\":\"plugin\",\"id\":\"home\",\"label\":\"Home\"},{\"type\":\"menu\",\"id\":\"importdata\",\"label\":\"Import data\",\"items\":[{\"type\":\"plugin\",\"id\":\"one-click-importer\",\"label\":\"Quick data import\"},{\"type\":\"plugin\",\"id\":\"importwizard\",\"label\":\"Advanced data import\"}]},{\"type\":\"plugin\",\"id\":\"navigator\",\"label\":\"Navigator\"},{\"type\":\"plugin\",\"id\":\"dataexplorer\",\"label\":\"Data Explorer\"},{\"type\":\"menu\",\"id\":\"dataintegration\",\"label\":\"Data Integration\",\"items\":[{\"type\":\"plugin\",\"id\":\"metadata-manager\",\"label\":\"Metadata Manager\"},{\"type\":\"plugin\",\"id\":\"mappingservice\",\"label\":\"Mapping Service\"},{\"type\":\"plugin\",\"id\":\"tagwizard\",\"label\":\"Tag Wizard\"},{\"type\":\"plugin\",\"id\":\"ontologymanager\",\"label\":\"Ontology manager\"}]},{\"type\":\"menu\",\"id\":\"plugins\",\"label\":\"Plugins\",\"items\":[{\"type\":\"plugin\",\"id\":\"searchAll\",\"label\":\"Search all data\"},{\"type\":\"plugin\",\"id\":\"swagger\",\"label\":\"API documentation\"},{\"type\":\"plugin\",\"id\":\"apps\",\"label\":\"App store\"},{\"type\":\"plugin\",\"id\":\"catalogue\",\"label\":\"Catalogue\"},{\"type\":\"plugin\",\"id\":\"feedback\",\"label\":\"Feedback\"},{\"type\":\"plugin\",\"id\":\"gavin-app\",\"label\":\"Gavin\"},{\"type\":\"plugin\",\"id\":\"jobs\",\"label\":\"Job overview\"},{\"type\":\"plugin\",\"id\":\"pathways\",\"label\":\"Pathways\"},{\"type\":\"plugin\",\"id\":\"questionnaires\",\"label\":\"Questionnaires\"},{\"type\":\"plugin\",\"id\":\"standardsregistry\",\"label\":\"Model registry\"},{\"type\":\"plugin\",\"id\":\"scripts\",\"label\":\"Scripts\"}]},{\"type\":\"menu\",\"id\":\"admin\",\"label\":\"Admin\",\"items\":[{\"type\":\"plugin\",\"id\":\"indexmanager\",\"label\":\"Index manager\"},{\"type\":\"plugin\",\"id\":\"logmanager\",\"label\":\"Log manager\"},{\"type\":\"plugin\",\"id\":\"menumanager\",\"label\":\"Menu Manager\"},{\"type\":\"plugin\",\"id\":\"permissionmanager\",\"label\":\"Permission Manager\"},{\"type\":\"plugin\",\"id\":\"scheduledjobs\",\"label\":\"Scheduled Jobs\"},{\"type\":\"plugin\",\"id\":\"settingsmanager\",\"label\":\"Settings\"},{\"type\":\"plugin\",\"id\":\"thememanager\",\"label\":\"Theme Manager\"},{\"type\":\"plugin\",\"id\":\"usermanager\",\"label\":\"User Manager\"}]},{\"type\":\"plugin\",\"id\":\"useraccount\",\"label\":\"Account\"}]}");
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testGetJobs() throws Exception
	{
		mockMvc.perform(get(SortaController.PLUGIN_URI_PREFIX + SortaController.ID + "/jobs").header(TOKEN_HEADER,
				getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
			   .andExpect(content().string("[]"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testInit() throws Exception
	{
		mockMvc.perform(
				get(SortaController.PLUGIN_URI_PREFIX + SortaController.ID).header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testMatchTask() throws Exception
	{
		mockMvc.perform(get(SortaController.PLUGIN_URI_PREFIX + SortaController.ID + "/newtask").header(TOKEN_HEADER,
				getAdminToken())).andExpect(status().isOk()).andExpect(view().name("sorta-match-view"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testMatchResult() throws Exception
	{
		mockMvc.perform(get(SortaController.PLUGIN_URI_PREFIX + SortaController.ID + "/result/1").header(TOKEN_HEADER,
				getAdminToken())).andExpect(status().isOk()).andExpect(view().name("sorta-match-view"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testCountMatchResult() throws Exception
	{
		addSortJobExecution();

		mockMvc.perform(get(SortaController.PLUGIN_URI_PREFIX + SortaController.ID + "/count/1").header(TOKEN_HEADER,
				getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(content().string("{\"numberOfMatched\":0,\"numberOfUnmatched\":0}"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testDeleteResult() throws Exception
	{
		mockMvc.perform(post(SortaController.PLUGIN_URI_PREFIX + SortaController.ID + "/delete/1").header(TOKEN_HEADER,
				getAdminToken()).with(csrf())).andExpect(status().isOk()).andExpect(view().name("sorta-match-view"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testUpdateThreshold() throws Exception
	{
		addSortJobExecution();

		mockMvc.perform(post(SortaController.PLUGIN_URI_PREFIX + SortaController.ID + "/threshold/1").param("threshold",
				String.valueOf(DEFAULT_THRESHOLD)).header(TOKEN_HEADER, getAdminToken()).with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME))
			   .andExpect(content().string(""));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testUploadMatchingFile() throws Exception
	{

		URL resourceUrl = Resources.getResource(SortaControllerIT.class, "/txt/sorta_test.txt");
		File file = new File(new URI(resourceUrl.toString()).getPath());

		byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

		MockMultipartFile sortTestFile = new MockMultipartFile("file", file.getName(), MULTIPART_FORM_DATA_VALUE, data);

		mockMvc.perform(
				fileUpload(SortaController.PLUGIN_URI_PREFIX + SortaController.ID + "/match/upload").file(sortTestFile)
																									.header(TOKEN_HEADER,
																											getAdminToken())
																									.
																											with(csrf())
																									.param("taskName",
																											"sortaTest")
																									.param("selectOntologies",
																											"/test"))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(content().string(""));
	}

	@AfterMethod
	public void afterMethod()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(TokenMetaData.TOKEN);
			dataService.deleteAll(SortaJobExecutionMetaData.SORTA_JOB_EXECUTION);

			User user = dataService.getRepository(UserMetaData.USER, User.class)
								   .query()
								   .eq(UserMetaData.USERNAME, SecurityITConfig.SUPERUSER_NAME)
								   .findOne();
			dataService.delete(UserMetaData.USER, user);
		});
	}

	@Configuration
	@EnableTransactionManagement(proxyTargetClass = true)
	@EnableAspectJAutoProxy
	@Import({ SortaTestConfig.class, OntologyTestConfig.class, GsonConfig.class, ScriptConfig.class,
			FileTestConfig.class, SettingsTestConfig.class, UiTestConfig.class, UtilTestConfig.class,
			JsTestConfig.class, DatabaseConfig.class, PostgreSqlConfiguration.class, DataTestConfig.class,
			SecurityITConfig.class })
	public static class Config
	{
	}

	private void addSortJobExecution()
	{
		String jobName = "testSortaJobExecution";
		String resultEntityName = idGenerator.generateId();
		SortaJobExecution execution = sortaJobExecutionFactory.create();
		execution.setName(jobName);
		execution.setIdentifier("1");
		execution.setSourceEntityName(SortaJobExecutionMetaData.SORTA_JOB_EXECUTION);
		execution.setResultEntityName(resultEntityName);
		execution.setOntologyIri("ontologyIri");
		execution.setDeleteUrl(ID + "/delete");
		execution.setThreshold(DEFAULT_THRESHOLD);
		execution.setUser(SecurityUtils.getCurrentUsername());

		dataService.add(SortaJobExecutionMetaData.SORTA_JOB_EXECUTION, execution);

		EntityType resultEntityType = EntityType.newInstance(matchingTaskContentMetaData, DEEP_COPY_ATTRS,
				attributeFactory);
		resultEntityType.setId(resultEntityName);
		resultEntityType.setPackage(null);
		resultEntityType.setAbstract(false);
		resultEntityType.addAttribute(attributeFactory.create()
													  .setName(INPUT_TERM)
													  .setDataType(XREF)
													  .setRefEntity(sortaJobExecutionFactory.getEntityType())
													  .setDescription("Reference to the input term")
													  .setNillable(false));
		resultEntityType.setLabel(jobName + " output");
		dataService.getMeta().addEntityType(resultEntityType);
	}

}
