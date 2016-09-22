package org.molgenis.data.idcard.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.idcard.model.IdCardEntity;
import org.molgenis.data.idcard.model.IdCardOrganization;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

import autovalue.shaded.com.google.common.common.primitives.Ints;
import org.molgenis.data.idcard.mapper.IdCardEntityMapper;
import static java.util.Objects.requireNonNull;

abstract public class AbstractIdCardClient<E extends IdCardEntity> implements IdCardClient<E>
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractIdCardClient.class);

	private final HttpClient httpClient;
	protected final IdCardIndexerSettings idCardIndexerSettings;
	private final IdCardEntityMapper<E> idCardEntityMapper;

	protected AbstractIdCardClient(HttpClient httpClient, IdCardIndexerSettings idCardIndexerSettings,
			IdCardEntityMapper<E> idCardBiobankMapper)
	{
		this.httpClient = requireNonNull(httpClient);
		this.idCardIndexerSettings = requireNonNull(idCardIndexerSettings);
		this.idCardEntityMapper = requireNonNull(idCardBiobankMapper);
	}

        abstract protected String getResourceURI(String id);
        abstract protected String getCollectionSelectionURI(Iterable<String> ids);
        abstract protected String getCollectionURI();

	@Override
	public Entity getIdCardEntity(String id)
	{
		return getIdCardEntity(id, idCardIndexerSettings.getApiTimeout());
	}

	@Override
	public Entity getIdCardEntity(String id, long timeout)
	{
		// Construct uri
		String uriBuilder = getResourceURI(id);

		return getIdCardResource(uriBuilder, new JsonResponseHandler<IdCardEntity>()
		{
			@Override
			public IdCardEntity deserialize(JsonReader jsonReader) throws IOException
			{
				return idCardEntityMapper.toIdCardBiobank(jsonReader);
			}
		}, timeout);
	}

	@Override
	public Iterable<Entity> getIdCardEntities(Iterable<String> ids)
	{
		return getIdCardEntities(ids, idCardIndexerSettings.getApiTimeout());
	}

	@Override
	public Iterable<Entity> getIdCardEntities(Iterable<String> ids, long timeout)
	{
		String uriBuilder = getCollectionSelectionURI(ids);

		return getIdCardResource(uriBuilder, new JsonResponseHandler<Iterable<Entity>>()
		{
			@Override
			public Iterable<Entity> deserialize(JsonReader jsonReader) throws IOException
			{
				return idCardEntityMapper.toIdCardBiobanks(jsonReader);
			}
		}, timeout);
	}

	private <T> T getIdCardResource(String url, ResponseHandler<T> responseHandler, long timeout)
	{

		HttpGet request = new HttpGet(url);
		request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		if (timeout != -1)
		{
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Ints.checkedCast(timeout))
					.setConnectionRequestTimeout(Ints.checkedCast(timeout)).setSocketTimeout(Ints.checkedCast(timeout))
					.build();
			request.setConfig(requestConfig);
		}
		try
		{
			LOG.info("Retrieving [" + url + "]");
			return httpClient.execute(request, responseHandler);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public Iterable<Entity> getIdCardEntities()
	{
		return getIdCardEntities(idCardIndexerSettings.getApiTimeout());
	}

	@Override
	public Iterable<Entity> getIdCardEntities(long timeout)
	{
		// Construct uri
		String uriBuilder = getCollectionURI();

		// Retrieve biobank ids
		Iterable<IdCardOrganization> idCardOrganizations = getIdCardResource(uriBuilder,
				new JsonResponseHandler<Iterable<IdCardOrganization>>()
				{
					@Override
					public Iterable<IdCardOrganization> deserialize(JsonReader jsonReader) throws IOException
					{
						return idCardEntityMapper.toIdCardOrganizations(jsonReader);
					}
				}, timeout);

		// Retrieve biobanks
		return this.getIdCardEntities(new Iterable<String>()
		{
			@Override
			public Iterator<String> iterator()
			{
				return StreamSupport.stream(idCardOrganizations.spliterator(), false)
						.map(IdCardOrganization::getOrganizationId).iterator();
			}
		}, timeout);
	}

	private static abstract class JsonResponseHandler<T> implements ResponseHandler<T>
	{
		@Override
		public T handleResponse(final HttpResponse response) throws ClientProtocolException, IOException
		{
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() < 100 || statusLine.getStatusCode() >= 300)
			{
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			if (entity == null)
			{
				throw new ClientProtocolException("Response contains no content");
			}

			try (JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), UTF_8))) {
				return deserialize(jsonReader);
			}
		}

		public abstract T deserialize(JsonReader jsonReader) throws IOException;
	}
}