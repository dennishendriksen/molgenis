package org.molgenis.data.idcard;

import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.idcard.client.IdCardClient;
import org.molgenis.data.idcard.model.IdCardEntity;
import org.molgenis.data.idcard.model.IdCardEntityMetaData;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.molgenis.data.support.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;

abstract public class IdCardRepository<E extends IdCardEntity> extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardRepository.class);

	private final IdCardEntityMetaData<E> idCardEntityMetaData;
	private final IdCardClient<E> idCardClient;
	private final ElasticsearchService elasticsearchService;
	protected final DataService dataService;
	private final IdCardIndexerSettings idCardIndexerSettings;

        abstract protected E getInstance();

        protected IdCardRepository(IdCardEntityMetaData<E> idCardEntityMetaData, IdCardClient<E> idCardClient,
			ElasticsearchService elasticsearchService, DataService dataService,
			IdCardIndexerSettings idCardIndexerSettings)
	{
		this.idCardEntityMetaData = idCardEntityMetaData;
		this.idCardClient = requireNonNull(idCardClient);
		this.elasticsearchService = requireNonNull(elasticsearchService);
		this.dataService = requireNonNull(dataService);
		this.idCardIndexerSettings = requireNonNull(idCardIndexerSettings);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return idCardClient.getIdCardEntities().iterator();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		Set<RepositoryCapability> repoCapabilities = new HashSet<>();
		repoCapabilities.add(AGGREGATEABLE);
		repoCapabilities.add(QUERYABLE);
		return repoCapabilities;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return idCardEntityMetaData;
	}

	@Override
	public long count(Query q)
	{
		return elasticsearchService.count(q, getEntityMetaData());
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return elasticsearchService.searchAsStream(q, getEntityMetaData());
	}

	@Override
	public Entity findOne(Query q)
	{
		Iterator<Entity> it = findAll(q).iterator();
		return it.hasNext() ? it.next() : null;
	}

	@Override
	public Entity findOne(Object id)
	{
		try
		{
			return idCardClient.getIdCardEntity(id.toString());
		}
		catch (RuntimeException e)
		{
			return createErrorIdCardBiobank(id);
		}
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return findOne(id);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return elasticsearchService.aggregate(aggregateQuery, getEntityMetaData());
	}

	@Override
	public void update(Entity entity)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void delete(Entity entity)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void deleteById(Object id)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void add(Entity entity)
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), WRITABLE.toString()));
	}

	@Override
	public void flush()
	{
		elasticsearchService.flush();
	}

	@Override
	public void rebuildIndex()
	{
		LOG.trace("Indexing ID-Card biobanks ...");
		Iterable<? extends Entity> entities = idCardClient
				.getIdCardEntities(idCardIndexerSettings.getIndexRebuildTimeout());

		EntityMetaData entityMeta = getEntityMetaData();
		if (!elasticsearchService.hasMapping(entityMeta))
		{
			elasticsearchService.createMappings(entityMeta);
		}
		elasticsearchService.index(entities, entityMeta, IndexingMode.UPDATE);
		LOG.debug("Indexed ID-Card biobanks");
	}

	private IdCardEntity createErrorIdCardBiobank(Object id)
	{
		IdCardEntity entity = getInstance();
		entity.set(IdCardEntity.ORGANIZATION_ID, id);
		entity.set(IdCardEntity.NAME, "Error loading data");
		return entity;
	}

	@Override
	public void create()
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), MANAGABLE.toString()));
	}

	@Override
	public void drop()
	{
		throw new UnsupportedOperationException(
				String.format("Repository [%s] is not %s", getName(), MANAGABLE.toString()));
	}
}
