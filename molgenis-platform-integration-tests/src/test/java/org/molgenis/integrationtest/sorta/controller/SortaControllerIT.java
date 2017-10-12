package org.molgenis.integrationtest.sorta.controller;

import org.molgenis.DatabaseConfig;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.integrationtest.data.DataTestConfig;
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
import org.molgenis.util.GsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.molgenis.integrationtest.utils.config.SecurityITConfig.SUPERUSER_NAME;
import static org.molgenis.integrationtest.utils.config.SecurityITConfig.SUPERUSER_ROLE;
import static org.molgenis.security.token.TokenExtractor.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = { SortaControllerIT.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class SortaControllerIT extends AbstractMolgenisIntegrationTests
{

	@Autowired
	private DataService dataService;
	@Autowired
	private UserFactory userFactory;

	public void beforeSpecificMethod()
	{
		User user = userFactory.create();
		user.setUsername(SecurityITConfig.SUPERUSER_NAME);
		user.setPassword(SecurityITConfig.SUPERUSER_NAME);
		user.setEmail("admin@molgenis.org");

		dataService.add(UserMetaData.USER, user);
	}

	@Test
	@WithMockUser(username = SUPERUSER_NAME, roles = SUPERUSER_ROLE)
	public void testGetJobs() throws Exception
	{
		MockHttpServletRequestBuilder request = get(
				SortaController.PLUGIN_URI_PREFIX + SortaController.ID + "/jobs/").header(TOKEN_HEADER,
				getAdminToken());

		mockMvc.perform(request)
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(""));
	}

	@BeforeClass
	@Override
	protected void springTestContextPrepareTestInstance() throws Exception
	{
		try
		{
			super.springTestContextPrepareTestInstance();
		}
		catch (Throwable err)
		{
			err.printStackTrace();
		}
	}

	@Configuration
	@EnableTransactionManagement(proxyTargetClass = true)
	@EnableAspectJAutoProxy
	@Import({ SortaTestConfig.class, OntologyTestConfig.class, GsonConfig.class, ScriptConfig.class,
			FileTestConfig.class, UiTestConfig.class, UtilTestConfig.class, JsTestConfig.class, DatabaseConfig.class,
			PostgreSqlConfiguration.class, DataTestConfig.class, SecurityITConfig.class })
	public static class Config
	{

	}

}
