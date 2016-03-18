package org.molgenis.data.elasticsearch.util;

import static org.elasticsearch.client.Requests.refreshRequest;

import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.index.MolgenisIndexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticsearchUtils implements MolgenisIndexUtil
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchUtils.class);
	private final Client client;

	public ElasticsearchUtils(Client client)
	{
		this.client = client;
	}

	public void deleteIndex(String index)
	{
		client.admin().indices().prepareDelete(index).execute().actionGet();
	}

	public boolean indexExists(String index)
	{
		return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
	}

	// Wait until elasticsearch is ready
	public void waitForYellowStatus()
	{
		client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
	}

	public void refreshIndex(String index)
	{
		LOG.info("Start refresh index [{}]", index);
		client.admin().indices().refresh(refreshRequest(index)).actionGet();
		LOG.info("End refresh index [{}]", index);
	}

	public void waitForCompletion(BulkProcessor bulkProcessor)
	{
		try
		{
			boolean isCompleted = bulkProcessor.awaitClose(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			if (!isCompleted)
			{
				throw new MolgenisDataException("Failed to complete bulk request within the given time");
			}
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

}
