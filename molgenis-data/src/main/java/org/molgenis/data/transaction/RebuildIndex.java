/**
 * 
 */
package org.molgenis.data.transaction;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Map;
import java.util.stream.StreamSupport;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.index.MolgenisIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author jjettenn
 *
 */
public class RebuildIndex implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(RebuildIndex.class);
	private final ApplicationContext ctx;
	
	public RebuildIndex(ApplicationContext ctx)
	{
		this.ctx = requireNonNull(ctx);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{	
		// final RepositoryCollection idCardRepositoryCollection; // Not used, do not remove
		// final RepositoryCollection elasticsearchRepositoryCollection; // Not used
		// idCardRepositoryCollection = repositoryCollections.get("idCardRepositoryCollection"); // Not used, do not
		// remove
		// elasticsearchRepositoryCollection = repositoryCollections.get("ElasticsearchRepositoryCollection"); // Not
		// used, do not remove
		runAsSystem(() -> {
			final RepositoryCollection mysqlRepositoryCollection;
			Map<String, RepositoryCollection> repositoryCollections = this.ctx
					.getBeansOfType(RepositoryCollection.class);
			mysqlRepositoryCollection = repositoryCollections.get("MysqlRepositoryCollection");

			final MolgenisIndexService molgenisIndexService;
			Map<String, MolgenisIndexService> molgenisIndexServices = ctx.getBeansOfType(MolgenisIndexService.class);
			molgenisIndexService = molgenisIndexServices.get("searchService");

			final DataService dataService;
			Map<String, DataService> dataServices = ctx.getBeansOfType(DataService.class);
			dataService = dataServices.get("dataService");

			LOG.info("Start rebuilding index");
			// This code reindex only MySql repositories TODO refresh more repositories

			// 1. Remove all
			StreamSupport.stream(mysqlRepositoryCollection.getEntityNames().spliterator(), false).filter(e -> {
				// filters abstract entities: "Owned", "authority", "settings_settings" and "Questionnaire"
					if (dataService.getRepository(e).getEntityMetaData().isAbstract()) return false;
					LOG.info("1. clean index [{}]", e);
					return true;
				}).forEach(el -> molgenisIndexService.delete(el));

			// 2. Add mapping
			StreamSupport
					.stream(mysqlRepositoryCollection.getEntityNames().spliterator(), false)
					.filter(e -> {
						// filters abstract entities: "Owned", "authority", "settings_settings" and
						// "Questionnaire"
						if (dataService.getRepository(e).getEntityMetaData().isAbstract()) return false;
						LOG.info("2. Add mapping into index [{}]", e);
						return true;
					})
					.forEach(
							el -> molgenisIndexService
									.createMappings(dataService.getRepository(el).getEntityMetaData()));

			// 3. Add entities
			StreamSupport
					.stream(mysqlRepositoryCollection.getEntityNames().spliterator(), false)
					.filter(e -> {
						// filters abstract entities: "Owned", "authority", "settings_settings" and
						// "Questionnaire"
						if (dataService.getRepository(e).getEntityMetaData().isAbstract()) return false;
						LOG.info("3. Add entities into index [{}]", e);
						return true;
					})
					.forEach(
							el -> {
								molgenisIndexService.add(dataService.getRepository(el), dataService.getRepository(el)
										.getEntityMetaData());
							});

			molgenisIndexService.getMolgenisIndexUtil().refreshIndex(MolgenisIndexService.DEFAULT_INDEX_NAME);
			LOG.info("End rebuild index");
		});
	}
}
