package org.molgenis.data.transaction;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import org.apache.commons.logging.LogFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.index.MolgenisIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * TransactionManager used by Molgenis.
 * 
 * TransactionListeners can be registered and will be notified on transaction begin, commit and rollback of transactions
 * that are not readonly.
 * 
 * Each transaction is given a unique transaction id.
 * 
 */
public class MolgenisTransactionManager extends DataSourceTransactionManager implements
		ApplicationListener<ContextRefreshedEvent>
{
	private static final long serialVersionUID = 1L;
	public static final String TRANSACTION_ID_RESOURCE_NAME = "transactionId";
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisTransactionManager.class);
	private final IdGenerator idGenerator;
	private final List<MolgenisTransactionListener> transactionListeners = new CopyOnWriteArrayList<>();
	private MolgenisIndexService dataIndexService;
	private RepositoryCollection idCardRepositoryCollection;
	private RepositoryCollection elasticsearchRepositoryCollection;
	private RepositoryCollection mysqlRepositoryCollection;
	private boolean bootstrapApplicationFinished = false;
	private DataService dataService;

	public MolgenisTransactionManager(IdGenerator idGenerator, DataSource dataSource)
	{
		super(dataSource);
		super.logger = LogFactory.getLog(DataSourceTransactionManager.class);
		setNestedTransactionAllowed(false);
		this.idGenerator = idGenerator;
	}

	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		runAsSystem(() -> {
			bootstrapApplication(ctx);
		});
	}

	private void bootstrapApplication(ApplicationContext ctx)
	{
		Map<String, MolgenisIndexService> molgenisIndexServices = ctx.getBeansOfType(MolgenisIndexService.class);
		dataIndexService = molgenisIndexServices.get("searchService");
		LOG.info("DataIndexService is allocated");

		// Get all expected repositories
		Map<String, RepositoryCollection> repositoryCollections = ctx.getBeansOfType(RepositoryCollection.class);
		idCardRepositoryCollection = repositoryCollections.get("idCardRepositoryCollection");
		elasticsearchRepositoryCollection = repositoryCollections.get("ElasticsearchRepositoryCollection");
		mysqlRepositoryCollection = repositoryCollections.get("MysqlRepositoryCollection");
		bootstrapApplicationFinished = true;

		Map<String, DataService> dataServices = ctx.getBeansOfType(DataService.class);
		this.dataService = dataServices.get("dataService");

		// Rebuild all
		this.refreshWholeIndex();
	}

	public void addTransactionListener(MolgenisTransactionListener transactionListener)
	{
		transactionListeners.add(transactionListener);
	}

	@Override
	protected Object doGetTransaction() throws TransactionException
	{
		Object dataSourceTransactionManager = super.doGetTransaction();

		String id;
		if (TransactionSynchronizationManager.hasResource(TRANSACTION_ID_RESOURCE_NAME))
		{
			id = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		}
		else
		{
			id = idGenerator.generateId().toLowerCase();
		}

		return new MolgenisTransaction(id, dataSourceTransactionManager);
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException
	{
		MolgenisTransaction molgenisTransaction = (MolgenisTransaction) transaction;
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Start transaction [{}]", molgenisTransaction.getId());
		}

		super.doBegin(molgenisTransaction.getDataSourceTransaction(), definition);

		if (!definition.isReadOnly())
		{
			TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, molgenisTransaction.getId());
			transactionListeners.forEach(j -> j.transactionStarted(molgenisTransaction.getId()));
		}
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException
	{
		MolgenisTransaction transaction = (MolgenisTransaction) status.getTransaction();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Commit transaction [{}]", transaction.getId());
		}

		DefaultTransactionStatus transactionStatus = new DefaultTransactionStatus(
				transaction.getDataSourceTransaction(), status.isNewTransaction(), status.isNewSynchronization(),
				status.isReadOnly(), status.isDebug(), status.getSuspendedResources());

		if (!status.isReadOnly())
		{
			transactionListeners.forEach(j -> j.commitTransaction(transaction.getId()));
		}
		
		super.doCommit(transactionStatus);
		// if (status.isNewTransaction()) this.refreshWholeIndex();

		this.refreshWholeIndex();
	}

	@SuppressWarnings("deprecation")
	private synchronized void refreshWholeIndex()
	{
		if (bootstrapApplicationFinished)
		{
			LOG.info("Start rebuild index");
			// This code reindex only mysql reposiotries
			
			// 1. Remove all
			StreamSupport.stream(mysqlRepositoryCollection.getEntityNames().spliterator(), false).filter(e -> {
				LOG.info("1. clean index [{}]", e);
				// filters abstract entities: "Owned", "authority", "settings_settings" and "Questionnaire"
					if (this.dataService.getRepository(e).getEntityMetaData().isAbstract()) return false;
					return true;
				}).forEach(el -> dataIndexService.delete(el));

			// 2. Add mapping
			StreamSupport
					.stream(mysqlRepositoryCollection.getEntityNames().spliterator(), false)
					.filter(e -> {
						LOG.info("2. Add mapping into index [{}]", e);
						// filters abstract entities: "Owned", "authority", "settings_settings" and "Questionnaire"
						if (this.dataService.getRepository(e).getEntityMetaData().isAbstract()) return false;
						return true;
					})
					.forEach(
							el -> dataIndexService.createMappings(mysqlRepositoryCollection.getRepository(el)
									.getEntityMetaData()));

			// 3. Add entities
			StreamSupport
					.stream(mysqlRepositoryCollection.getEntityNames().spliterator(), false)
					.filter(e -> {
						LOG.info("3. Add entities into index [{}]", e);
						// filters abstract entities: "Owned", "authority", "settings_settings" and "Questionnaire"
						if (this.dataService.getRepository(e).getEntityMetaData().isAbstract()) return false;
						return true;
					})
					.forEach(
							el -> {
								dataIndexService.add(this.dataService.getRepository(el), this.dataService
										.getRepository(el).getEntityMetaData());
							});

			dataIndexService.getMolgenisIndexUtil().refreshIndex(MolgenisIndexService.DEFAULT_INDEX_NAME);
			LOG.info("End rebuild index");
		}
		else
		{
			LOG.info("Skip rebuilding index, bootstrap application is not finished");
		}

	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException
	{
		MolgenisTransaction transaction = (MolgenisTransaction) status.getTransaction();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Rollback transaction [{}]", transaction.getId());
		}

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(
				transaction.getDataSourceTransaction(), status.isNewTransaction(), status.isNewSynchronization(),
				status.isReadOnly(), status.isDebug(), status.getSuspendedResources());

		if (!status.isReadOnly())
		{
			transactionListeners.forEach(j -> j.rollbackTransaction(transaction.getId()));
		}

		super.doRollback(jpaTransactionStatus);
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status)
	{
		MolgenisTransaction transaction = (MolgenisTransaction) status.getTransaction();

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(
				transaction.getDataSourceTransaction(), status.isNewTransaction(), status.isNewSynchronization(),
				status.isReadOnly(), status.isDebug(), status.getSuspendedResources());

		super.doSetRollbackOnly(jpaTransactionStatus);
	}

	@Override
	protected boolean isExistingTransaction(Object transaction)
	{
		return super.isExistingTransaction(((MolgenisTransaction) transaction).getDataSourceTransaction());
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction)
	{
		MolgenisTransaction molgenisTransaction = (MolgenisTransaction) transaction;
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Cleanup transaction [{}]", molgenisTransaction.getId());
		}

		super.doCleanupAfterCompletion(molgenisTransaction.getDataSourceTransaction());

		TransactionSynchronizationManager.unbindResourceIfPossible(TRANSACTION_ID_RESOURCE_NAME);
	}

	@Override
	protected Object doSuspend(Object transaction)
	{
		MolgenisTransaction molgenisTransaction = (MolgenisTransaction) transaction;
		return super.doSuspend(molgenisTransaction.getDataSourceTransaction());
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources)
	{
		MolgenisTransaction molgenisTransaction = (MolgenisTransaction) transaction;
		super.doResume(molgenisTransaction.getDataSourceTransaction(), suspendedResources);
	}

}
