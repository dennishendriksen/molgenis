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
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;

import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
public abstract class AbstractMolgenisIntegrationTests extends AbstractTestNGSpringContextTests
		implements ApplicationListener<ContextRefreshedEvent>
{

	private Logger LOG = LoggerFactory.getLogger(AbstractMolgenisIntegrationTests.class);

	@Autowired
	protected WebApplicationContext context;
	@Autowired
	protected FilterChainProxy springSecurityFilterChain;

	protected MockMvc mockMvc;

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
		mockMvc = webAppContextSetup(context).addFilter(springSecurityFilterChain).alwaysDo(print()).build();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[] { new ClassPathResource("/conf/molgenis.properties") };
		pspc.setLocations(resources);
		pspc.setFileEncoding("UTF-8");
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		pspc.setNullValue("@null");
		return pspc;
	}

	// FIXME The bootstrapping of the data platform should be delegated to a specific bootstrapper so that updates
	// are reflected in the test
	@Override
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
