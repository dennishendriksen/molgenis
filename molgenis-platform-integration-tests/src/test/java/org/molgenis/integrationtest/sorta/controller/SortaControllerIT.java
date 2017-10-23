package org.molgenis.integrationtest.sorta.controller;

import com.google.common.io.Resources;
import org.molgenis.DatabaseConfig;
import org.molgenis.auth.TokenMetaData;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.integrationtest.data.DataTestConfig;
import org.molgenis.integrationtest.file.FileTestConfig;
import org.molgenis.integrationtest.js.JsTestConfig;
import org.molgenis.integrationtest.ontology.sorta.SortaTestConfig;
import org.molgenis.integrationtest.script.ScriptConfig;
import org.molgenis.integrationtest.util.UtilTestConfig;
import org.molgenis.integrationtest.utils.AbstractMolgenisIntegrationTests;
import org.molgenis.integrationtest.utils.StringArgumentCaptor;
import org.molgenis.integrationtest.utils.config.SecurityITConfig;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.importer.OntologyImportService;
import org.molgenis.ontology.importer.repository.OntologyRepositoryCollection;
import org.molgenis.ontology.sorta.controller.SortaController;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;
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

import static org.molgenis.integrationtest.utils.config.SecurityITConfig.SUPERUSER_NAME;
import static org.molgenis.ontology.sorta.controller.SortaController.DEFAULT_THRESHOLD;
import static org.molgenis.ontology.sorta.controller.SortaController.MATCH_VIEW_NAME;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.token.TokenExtractor.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
	private DataService dataService;
	@Autowired
	private UserFactory userFactory;
	@Autowired
	private OntologyImportService ontologyImportService;

	@Autowired
	private AppSettings appSettings;

	public void beforeMethod()
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

	/**
	 * <p>Get jobid from system.</p>
	 *
	 * @return job id
	 * @throws Exception
	 */
	private String getJobId() throws Exception
	{
		StringArgumentCaptor argumentCaptor = new StringArgumentCaptor();

		mockMvc.perform(get(SortaController.URI + "/jobs").header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(jsonPath("$[0].identifier").value(argumentCaptor));

		return argumentCaptor.capture();
	}

	@Test(groups = "withData")
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
			   .andExpect(view().name("redirect:/menu/main/" + SortaController.ID));
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testGetJobs() throws Exception
	{
		mockMvc.perform(get(SortaController.URI + "/jobs"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_VALUE))
			   .andExpect(jsonPath("$[0].name").value("sortaTest"));

	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testMatchResult() throws Exception
	{
		mockMvc.perform(get(SortaController.URI + "/result/" + getJobId()).header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(SortaController.MATCH_VIEW_NAME));

	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testCountMatchResult() throws Exception
	{
		mockMvc.perform(get(SortaController.URI + "/count/" + getJobId()).header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(content().string("{\"numberOfMatched\":0,\"numberOfUnmatched\":45}"));
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testUpdateThreshold() throws Exception
	{

		mockMvc.perform(post(SortaController.URI + "/threshold/" + getJobId()).param("threshold",
				String.valueOf(DEFAULT_THRESHOLD)).header(TOKEN_HEADER, getAdminToken()).with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));
	}

	@Test(groups = { "withData" }, dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testDownloadResult() throws Exception
	{
		mockMvc.perform(get(SortaController.URI + "/match/download/" + getJobId()).header(TOKEN_HEADER, getAdminToken())
																				  .with(csrf()))
			   .andExpect(status().isOk());
	}

	@Test(dependsOnGroups = "withData")
	@WithMockUser(username = SUPERUSER_NAME, roles = SecurityITConfig.SUPERUSER_ROLE)
	public void testDeleteResult() throws Exception
	{
		mockMvc.perform(
				post(SortaController.URI + "/delete/" + getJobId()).header(TOKEN_HEADER, getAdminToken()).with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(SortaController.MATCH_VIEW_NAME));
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
	@Import({ SortaTestConfig.class, OntologyTestConfig.class, ScriptConfig.class, FileTestConfig.class,
			UtilTestConfig.class, JsTestConfig.class, DatabaseConfig.class, PostgreSqlConfiguration.class,
			DataTestConfig.class })
	public static class Config
	{
	}

}
