package org.molgenis.integrationtest.sorta.controller;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.molgenis.DatabaseConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.rest.EntityPager;
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
import org.molgenis.ontology.sorta.controller.SortaAnonymousController;
import org.molgenis.ontology.sorta.request.SortaServiceRequest;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.molgenis.integrationtest.utils.config.SecurityITConfig.ANONYMOUSE_USER;
import static org.molgenis.ontology.sorta.controller.SortaAnonymousController.*;
import static org.molgenis.security.token.TokenExtractor.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = { SortaAnonymousControllerIT.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class SortaAnonymousControllerIT extends AbstractMolgenisIntegrationTests
{
	@Autowired
	private DataService dataService;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private SortaITUtils sortaITUtils;

	private String expression = "";

	public void beforeMethod()
	{
		sortaITUtils.addUserIfExists(SecurityITConfig.ANONYMOUSE_USER);
		appSettings.setMenu(
				"{\"type\":\"menu\",\"id\":\"main\",\"label\":\"Home\",\"items\":[{\"type\":\"plugin\",\"id\":\"sortaservice\",\"label\":\"SORTA\",\"params\":\"\"}]}");
	}

	/**
	 * <p>Get jobid from system.</p>
	 *
	 * @return job id
	 * @throws Exception
	 */
	private String getValueFromJobsResponse(String expression) throws Exception
	{
		StringArgumentCaptor argumentCaptor = new StringArgumentCaptor();
		if (this.expression == null || !this.expression.equals(expression))
		{
			mockMvc.perform(get(URI + "/jobs").header(TOKEN_HEADER, getAdminToken()))
				   .andExpect(jsonPath("$[0]" + expression).value(argumentCaptor));
		}
		return argumentCaptor.capture();
	}

	@Test(groups = "withData")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testUploadMatchingFile() throws Exception
	{
		sortaITUtils.addOntologies();

		URL resourceUrl = Resources.getResource(SortaAnonymousControllerIT.class, "/txt/sorta_test.txt");
		File file = new File(new URI(resourceUrl.toString()).getPath());

		byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

		MockMultipartFile sortTestFile = new MockMultipartFile("file", file.getName(), MULTIPART_FORM_DATA_VALUE, data);

		mockMvc.perform(fileUpload(URI + "/match/upload").file(sortTestFile)
														 .header(TOKEN_HEADER, getAdminToken())
														 .
																 with(csrf())
														 .param("taskName", "sortaTest")
														 .param("selectOntologies",
																 "http://www.biobankconnect.org/ontologies/2014/2/custom_ontology"))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(view().name("redirect:/menu/main/" + ID));
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testGetJobs() throws Exception
	{
		mockMvc.perform(get(URI + "/jobs"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_VALUE))
			   .andExpect(jsonPath("$[0].name").value("sortaTest"));

	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testMatchResult() throws Exception
	{
		mockMvc.perform(
				get(URI + "/result/" + getValueFromJobsResponse("identifier")).header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));

	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testCountMatchResult() throws Exception
	{
		mockMvc.perform(
				get(URI + "/count/" + getValueFromJobsResponse("identifier")).header(TOKEN_HEADER, getAdminToken()))
			   .andExpect(status().isOk())
			   .andExpect(content().string("{\"numberOfMatched\":0,\"numberOfUnmatched\":45}"));
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testRetrieveMatch() throws Exception
	{

		String jobId = getValueFromJobsResponse("identifier");
		String ontologyIri = getValueFromJobsResponse("ontologyIri");
		String filterQuery = "test";

		SortaServiceRequest request = new SortaServiceRequest(jobId, ontologyIri, filterQuery, false,
				new EntityPager(0, 10, new Long(0), null));

		mockMvc.perform(post(URI + "/match/retrieve/").content(new Gson().toJson(request))
													  .
															  header(TOKEN_HEADER, getAdminToken())
													  .with(csrf())
													  .contentType(APPLICATION_JSON_VALUE)
													  .accept(APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testUpdateThreshold() throws Exception
	{

		mockMvc.perform(post(URI + "/threshold/" + getValueFromJobsResponse("identifier")).param("threshold",
				String.valueOf(SortaAnonymousController.DEFAULT_THRESHOLD))
																						  .header(TOKEN_HEADER,
																								  getAdminToken())
																						  .with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testSearchResult() throws Exception
	{

		Map<String, Object> request = new HashMap<>();
		request.put("queryString", "test");
		request.put(OntologyMetaData.ONTOLOGY_IRI, getValueFromJobsResponse("ontologyIri"));

		mockMvc.perform(post(URI + "/search/").content(new Gson().toJson(request))
											  .header(TOKEN_HEADER, getAdminToken())
											  .contentType(APPLICATION_JSON_VALUE)
											  .accept(APPLICATION_JSON_VALUE)
											  .with(csrf())).andExpect(status().isOk());
	}

	@Test(groups = "withData", dependsOnMethods = "testUploadMatchingFile")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testDownloadResult() throws Exception
	{
		mockMvc.perform(get(URI + "/match/download/" + getValueFromJobsResponse("identifier")).header(TOKEN_HEADER,
				getAdminToken()).with(csrf())).andExpect(status().isOk());
	}

	@Test(dependsOnGroups = "withData")
	@WithMockUser(username = ANONYMOUSE_USER, roles = ANONYMOUSE_USER)
	public void testDeleteResult() throws Exception
	{
		mockMvc.perform(
				post(URI + "/delete/" + getValueFromJobsResponse("identifier")).header(TOKEN_HEADER, getAdminToken())
																			   .with(csrf()))
			   .andExpect(status().isOk())
			   .andExpect(view().name(MATCH_VIEW_NAME));
	}

	@AfterClass
	public void afterClass()
	{
		sortaITUtils.cleanUp();
	}

	@Configuration
	@EnableTransactionManagement(proxyTargetClass = true)
	@EnableAspectJAutoProxy
	@Import({ SortaTestConfig.class, OntologyTestConfig.class, ScriptConfig.class, FileTestConfig.class,
			UtilTestConfig.class, JsTestConfig.class, DatabaseConfig.class, PostgreSqlConfiguration.class,
			DataTestConfig.class, SortaITUtils.class })
	public static class Config
	{
	}
}
