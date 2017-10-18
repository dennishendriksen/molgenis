package org.molgenis.integrationtest.utils.config;

import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.SystemRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.jobs.JobFactoryRegistrar;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistrar;
import org.molgenis.data.platform.bootstrap.SystemEntityTypeBootstrapper;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistryPopulator;
import org.molgenis.data.settings.SettingsPopulator;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;

@Component
public class BootstrapTestUtils
{

	private Logger LOG = LoggerFactory.getLogger(BootstrapTestUtils.class);

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
	@Autowired
	private SettingsPopulator settingsPopulator;

	public void bootstrap(ContextRefreshedEvent event)
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

					// Settings to database instead of using TestAppSettings
					LOG.trace("Populating settings entities ...");
					settingsPopulator.initialize(event);
					LOG.trace("Populated settings entities");

					LOG.trace("Populating settings entities ...");
					settingsPopulator.initialize(event);
					LOG.trace("Populated settings entities");

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
