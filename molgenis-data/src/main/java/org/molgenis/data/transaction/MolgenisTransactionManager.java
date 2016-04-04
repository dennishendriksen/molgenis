package org.molgenis.data.transaction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.apache.commons.logging.LogFactory;
import org.molgenis.data.IdGenerator;
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
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisTransactionManager.class);
	private static final long serialVersionUID = 1L;
	public static final String TRANSACTION_ID_RESOURCE_NAME = "transactionId";
	private final IdGenerator idGenerator;
	private final List<MolgenisTransactionListener> transactionListeners = new CopyOnWriteArrayList<>();
	private ApplicationContext ctx = null;
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
	private static volatile Future<?> rebuildIndexJob = null;
	private static volatile RebuildIndex rebuildIndex = null;

	public MolgenisTransactionManager(IdGenerator idGenerator, DataSource dataSource)
	{
		super(dataSource);
		super.logger = LogFactory.getLog(DataSourceTransactionManager.class);
		setNestedTransactionAllowed(false);
		this.idGenerator = idGenerator;
	}

	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		this.ctx = event.getApplicationContext();
		this.refreshWholeIndexSychronized(); // Synchronized
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

		this.refreshWholeIndexASychronized();
	}

	/**
	 * A-Synchronized
	 * 
	 * TODO implement fine grained rebuilding index strategy TODO Implement a mechanism to stop a thread or wait on
	 * thread to not execute the indexing twice at the same time.
	 */
	private synchronized void refreshWholeIndexASychronized()
	{
		if (null != this.ctx)
		{
			if (rebuildIndexJob != null && !rebuildIndexJob.isDone() && null != rebuildIndex)
			{
				rebuildIndex.stop();
				LOG.info("### --- Rebuilding index is stoped! --- ###");
				try
				{
					rebuildIndexJob.get();
				}
				catch (InterruptedException | ExecutionException e)
				{
					LOG.info("rebuildIndexJob.get() exception:", e);
				}
				LOG.info("### --- Rebuilding index is canceled! --- ###");
			}
			rebuildIndex = new RebuildIndex(this.ctx);
			rebuildIndexJob = EXECUTOR_SERVICE.submit(rebuildIndex);
		}
		else
		{
			LOG.info("Skip rebuilding index, bootstrap application is not finished");
		}
	}

	/**
	 * Synchronized
	 * 
	 * TODO implement fine grained rebuilding index strategy
	 */
	private synchronized void refreshWholeIndexSychronized()
	{
		if (null != this.ctx)
		{
			new RebuildIndex(this.ctx).run();
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
