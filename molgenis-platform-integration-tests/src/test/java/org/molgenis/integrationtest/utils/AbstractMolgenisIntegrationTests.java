package org.molgenis.integrationtest.utils;

import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.SystemRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.jobs.JobFactoryRegistrar;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistrar;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistryPopulator;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.integrationtest.utils.config.SecurityITConfig;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.molgenis.security.core.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
@TestPropertySource("/conf/molgenis.properties")
public abstract class AbstractMolgenisIntegrationTests extends AbstractTestNGSpringContextTests
{

	private Logger LOG = LoggerFactory.getLogger(AbstractMolgenisIntegrationTests.class);

	@Autowired
	protected WebApplicationContext context;
	@Autowired
	protected FilterChainProxy springSecurityFilterChain;

	protected MockMvc mockMvc;

	@Autowired
	private TokenService tokenService;
	@Autowired
	private RepositoryCollectionBootstrapper repoCollectionBootstrapper;
	@Autowired
	private SystemEntityTypeRegistrar systemEntityTypeRegistrar;
	@Autowired
	private SystemPackageRegistrar systemPackageRegistrar;
	@Autowired
	private EntityFactoryRegistrar entityFactoryRegistrar;
	@Autowired
	private SystemEntityTypeBootstrapper systemEntityTypeBootstrapper;
	@Autowired
	private SystemRepositoryDecoratorFactoryRegistrar systemRepositoryDecoratorFactoryRegistrar;
	@Autowired
	private JobFactoryRegistrar jobFactoryRegistrar;
	@Autowired
	private TransactionManager transactionManager;

	@BeforeMethod
	public void beforeMethod()
	{

		mockMvc = webAppContextSetup(context).addFilters(springSecurityFilterChain).alwaysDo(print()).build();

		ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
		when(event.getApplicationContext()).thenReturn(context);
		onApplicationEvent(event);

		beforeSpecificMethod();
	}

	/**
	 * Use this method as a beforeMethod in the integration test
	 */
	public abstract void beforeSpecificMethod();

	protected String getAdminToken()
	{
		return tokenService.generateAndStoreToken(SecurityITConfig.SUPERUSER_NAME, "REST token");
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
	{
		return new PropertySourcesPlaceholderConfigurer();
	}

	// FIXME The bootstrapping of the data platform should be delegated to a specific bootstrapper so that updates
	// are reflected in the test
	@WithMockUser
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		TransactionTemplate transactionTemplate = new TransactionTemplate();
		transactionTemplate.setTransactionManager(transactionManager);
		transactionTemplate.execute((action) ->
		{
			try
			{
				RunAsSystemAspect.runAsSystem(() ->
				{
					LOG.info("Bootstrapping registries ...");
					LOG.trace("Registering repository collections ...");
					repoCollectionBootstrapper.bootstrap(event, POSTGRESQL);
					LOG.trace("Registered repository collections");

					LOG.trace("Registering system entity meta data ...");
					systemEntityTypeRegistrar.register(event);
					LOG.trace("Registered system entity meta data");

					LOG.trace("Registering system packages ...");
					systemPackageRegistrar.register(event);
					LOG.trace("Registered system packages");

					LOG.trace("Registering entity factories ...");
					entityFactoryRegistrar.register(event);
					LOG.trace("Registered entity factories");

					LOG.trace("Registering entity factories ...");
					systemRepositoryDecoratorFactoryRegistrar.register(event);
					LOG.trace("Registered entity factories");
					LOG.debug("Bootstrapped registries");

					LOG.trace("Bootstrapping system entity types ...");
					systemEntityTypeBootstrapper.bootstrap(event);
					LOG.debug("Bootstrapped system entity types");

					LOG.trace("Registering job factories ...");
					jobFactoryRegistrar.register(event);
					LOG.trace("Registered job factories");

					event.getApplicationContext().getBean(EntityTypeRegistryPopulator.class).populate();
				});
			}
			catch (Exception unexpected)
			{
				LOG.error("Error bootstrapping tests!", unexpected);
				throw new RuntimeException(unexpected);
			}
			return (Void) null;
		});
	}
}
