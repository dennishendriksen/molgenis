package org.molgenis.integrationtest.sorta.controller;

import com.google.common.io.Resources;
import org.molgenis.DatabaseConfig;
import org.molgenis.auth.TokenMetaData;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
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
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.importer.OntologyImportService;
import org.molgenis.ontology.importer.repository.OntologyRepositoryCollection;
import org.molgenis.ontology.sorta.controller.SortaController;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.molgenis.ontology.sorta.job.SortaJobExecutionFactory;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.GsonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testng.annotations.AfterClass;
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

	private Logger LOG = LoggerFactory.getLogger(SortaControllerIT.class);

	@Autowired
	private AutowireCapableBeanFactory autowireCapableBeanFactory;

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
	private OntologyImportService ontologyImportService;

	@Autowired
	private AppSettings appSettings;

	public void beforeITMethod()
	{
		addUserIfExists();
		appSettings.setMenu(
				"{\"type\":\"menu\",\"id\":\"main\",\"label\":\"Home\",\"items\":[{\"type\":\"plugin\",\"id\":\"sortaservice\",\"label\":\"SORTA\",\"params\":\"\"}]}");

	}

	private void addUserIfExists()
	{
		User existingUser = dataService.getRepository(UserMetaData.USER, User.class)
									   .query()
									   .eq(UserMetaData.USERNAME, SUPERUSER_NAME)
									   .findOne();
		if (existingUser == null)
		{
			User user = userFactory.create();
			user.setUsername(SecurityITConfig.SUPERUSER_NAME);
			user.setPassword(SecurityITConfig.SUPERUSER_NAME);
			user.setEmail("admin@molgenis.org");
			dataService.add(UserMetaData.USER, user);
		}
	}

	private void addOntologies()
	{
		runAsSystem(() ->
		{
			try
			{
				URL resourceUrl = Resources.getResource(SortaControllerIT.class, "/owl/biobank_ontology.owl.zip");
				File file = new File(new URI(resourceUrl.toString()).getPath());

				OntologyRepositoryCollection ontologyRepositoryCollection = new OntologyRepositoryCollection(file);
				autowireCapableBeanFactory.autowireBeanProperties(ontologyRepositoryCollection,
						AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
				ontologyRepositoryCollection.init();
				ontologyImportService.doImport(ontologyRepositoryCollection, DatabaseAction.ADD, "ontologiesTest");

			}
			catch (Exception err)
			{
				LOG.error("Error occurred during specific before method. ", err);
			}
		});

	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testUploadMatchingFile() throws Exception
	{
		addOntologies();
		
		URL resourceUrl = Resources.getResource(SortaControllerIT.class, "/txt/sorta_test.txt");
		File file = new File(new URI(resourceUrl.toString()).getPath());

		byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

		MockMultipartFile sortTestFile = new MockMultipartFile("file", file.getName(), MULTIPART_FORM_DATA_VALUE, data);

		mockMvc.perform(fileUpload(SortaController.URI + "/match/upload").file(sortTestFile)
																		 .header(TOKEN_HEADER, getAdminToken())
																		 .
																				 with(csrf())
																		 .param("taskName", "sortaTest")
																		 .param("selectOntologies",
																				 "http://www.biobankconnect.org/ontologies/2014/2/custom_ontology"))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(content().string(""));
	}

	@Test(dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testGetJobs() throws Exception
	{
		mockMvc.perform(get(SortaController.URI + "/jobs").header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
			   .andExpect(content().string("[]"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testInit() throws Exception
	{
		mockMvc.perform(get(SortaController.URI).header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testMatchTask() throws Exception
	{
		mockMvc.perform(get(SortaController.URI + "/newtask").header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(view().name("sorta-match-view"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testMatchResult() throws Exception
	{
		mockMvc.perform(get(SortaController.URI + "/result/1").header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(view().name("sorta-match-view"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testCountMatchResult() throws Exception
	{
		addSortJobExecution();

		mockMvc.perform(get(SortaController.URI + "/count/1").header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(content().string("{\"numberOfMatched\":0,\"numberOfUnmatched\":0}"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testDeleteResult() throws Exception
	{
		mockMvc.perform(post(SortaController.URI + "/delete/1").header(TOKEN_HEADER, getAdminToken()).with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name("sorta-match-view"));
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testUpdateThreshold() throws Exception
	{
		addSortJobExecution();

		mockMvc.perform(post(SortaController.URI + "/threshold/1").param("threshold", String.valueOf(DEFAULT_THRESHOLD))
																  .header(TOKEN_HEADER, getAdminToken())
																  .with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME))
			   .andExpect(content().string(""));
	}

	@AfterClass
	public void afterClass()
	{
		runAsSystem(() ->
		{
			dataService.deleteAll(TokenMetaData.TOKEN);
			dataService.deleteAll(SortaJobExecutionMetaData.SORTA_JOB_EXECUTION);
			dataService.deleteAll(OntologyTermMetaData.ONTOLOGY_TERM);
			dataService.deleteAll(OntologyMetaData.ONTOLOGY);

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
