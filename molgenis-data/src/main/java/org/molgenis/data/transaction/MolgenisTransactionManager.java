package org.molgenis.data.transaction;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import org.apache.commons.logging.LogFactory;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.index.MolgenisIndexService;
import org.molgenis.data.index.MolgenisIndexUtil;
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
	private final List<MolgenisTransactionListener> transactionListeners = new CopyOnWriteArrayList<>(); // FIX
	private MolgenisIndexService dataIndexService;
	private boolean bootstrapApplicationFinished = false;

	public MolgenisTransactionManager(IdGenerator idGenerator, DataSource dataSource)
	{
		super(dataSource);
		super.logger = LogFactory.getLog(DataSourceTransactionManager.class);
		setNestedTransactionAllowed(false);
		this.idGenerator = idGenerator;
	}

	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		bootstrapApplicationFinished = true;
		ApplicationContext ctx = event.getApplicationContext();
		runAsSystem(() -> {
			bootstrapApplication(ctx);
		});
		LOG.info("DataIndexService is allocated");
	}

	private void bootstrapApplication(ApplicationContext ctx)
	{
		Map<String, MolgenisIndexService> molgenisIndexServices = ctx.getBeansOfType(MolgenisIndexService.class);
		dataIndexService = molgenisIndexServices.get("searchService");
		dataIndexService.getMolgenisIndexUtil().refreshIndex(MolgenisIndexService.DEFAULT_INDEX_NAME);
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

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(
				transaction.getDataSourceTransaction(), status.isNewTransaction(), status.isNewSynchronization(),
				status.isReadOnly(), status.isDebug(), status.getSuspendedResources());

		if (!status.isReadOnly())
		{
			transactionListeners.forEach(j -> j.commitTransaction(transaction.getId()));
		}
		
		super.doCommit(jpaTransactionStatus);
		
		// TODO
		// 1. Implement remove index
		// 2. Implement rebuild the whole Postgresql index

		if (bootstrapApplicationFinished)
		{
			dataIndexService.getMolgenisIndexUtil().refreshIndex(MolgenisIndexUtil.DEFAULT_INDEX_NAME);
		}
		else
		{
			LOG.info("Skip refresh index bootstrap application is not finished");
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

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(transaction.getDataSourceTransaction(),
				status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
				status.getSuspendedResources());

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

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(transaction.getDataSourceTransaction(),
				status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
				status.getSuspendedResources());

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
