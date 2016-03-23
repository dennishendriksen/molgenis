package org.molgenis.data.elasticsearch;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.QueryUtils;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository that wraps an existing repository and analyze and route the queries
 */
public class IndexedRepositoryQueryAnalyzerDecorator implements Repository
{
	private final Repository decoratedRepo;
	private final SearchService elasticSearchService;

	public IndexedRepositoryQueryAnalyzerDecorator(Repository decoratedRepo, SearchService elasticSearchService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.elasticSearchService = requireNonNull(elasticSearchService);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
	}

	@Override
	@Transactional
	public void add(Entity entity)
	{
		decoratedRepo.add(entity);
	}

	@Override
	@Transactional
	public Integer add(Stream<? extends Entity> entities)
	{
		return decoratedRepo.add(entities);
	}

	@Override
	public void flush()
	{
		decoratedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepo.clearCache();
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		decoratedRepo.update(entity);
	}

	@Override
	@Transactional
	public void update(Stream<? extends Entity> entities)
	{
		decoratedRepo.update(entities);
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		decoratedRepo.delete(entity);
	}

	@Override
	@Transactional
	public void delete(Stream<? extends Entity> entities)
	{
		decoratedRepo.delete(entities);
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		decoratedRepo.deleteById(id);
	}

	@Override
	@Transactional
	public void deleteById(Stream<Object> ids)
	{
		decoratedRepo.deleteById(ids);
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		decoratedRepo.deleteAll();
	}

	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepo.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decoratedRepo.findOne(id, fetch);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decoratedRepo.findOne(q);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decoratedRepo.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepo.findAll(ids, fetch);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		if (QueryUtils.containsOperator(q, Operator.SEARCH)) return elasticSearchService.searchAsStream(q,
				getEntityMetaData());
		return decoratedRepo.findAll(q);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepo.iterator();
	}

	@Override
	public Stream<Entity> stream(Fetch fetch)
	{
		return decoratedRepo.stream(fetch);
	}

	@Override
	public void rebuildIndex()
	{
		elasticSearchService.rebuildIndex(decoratedRepo, getEntityMetaData());
	}

	@Override
	public void create()
	{
		if (!decoratedRepo.getCapabilities().contains(MANAGABLE))
		{
			throw new MolgenisDataAccessException("Repository '" + decoratedRepo.getName() + "' is not Manageable");
		}
		decoratedRepo.create();
	}

	@Override
	public void drop()
	{
		if (!decoratedRepo.getCapabilities().contains(MANAGABLE))
		{
			throw new MolgenisDataAccessException("Repository '" + decoratedRepo.getName() + "' is not Manageable");
		}
		decoratedRepo.drop();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepo.getCapabilities();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepo.removeEntityListener(entityListener);
	}

	@Override
	public long count()
	{
		return decoratedRepo.count();
	}

	@Override
	public long count(Query q)
	{
		if (QueryUtils.containsOperator(q, Operator.SEARCH)) return elasticSearchService.count(q, getEntityMetaData());
		return decoratedRepo.count(q);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public Query query()
	{
		return decoratedRepo.query();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepo.aggregate(aggregateQuery);
	}
}
