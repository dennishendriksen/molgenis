package org.molgenis.data.entity;

import static java.util.Objects.requireNonNull;

import java.util.Date;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.EntityMetaData;

public class LazyEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final EntityMetaData entityMetaData;
	private final DataService dataService;
	private final Object id;

	private Entity entity;

	public LazyEntity(EntityMetaData entityMetaData, DataService dataService, Object id)
	{
		this.entityMetaData = requireNonNull(entityMetaData);
		this.dataService = requireNonNull(dataService);
		this.id = requireNonNull(id);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Object getIdValue()
	{
		return id;
	}

	@Override
	public Object get(AttributeMetaData attr)
	{
		return null;
	}

	@Override
	public Object get(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(AttributeMetaData attr)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getInt(AttributeMetaData attr)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getInt(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getLong(AttributeMetaData attr)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getLong(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getBoolean(AttributeMetaData attr)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getBoolean(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getDouble(AttributeMetaData attr)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getDouble(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDate(AttributeMetaData attr)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDate(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getEntity(AttributeMetaData attr)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getEntity(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> E getEntity(AttributeMetaData attr, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity> E getEntity(String attrName, Class<E> clazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityCollection getEntities(AttributeMetaData attr)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityCollection getEntities(String attrName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private Entity getLazyLoadedEntity()
	{
		// FIXME
		return null;
		// if (entity == null)
		// {
		// entity = dataService.findOne(getEntityMetaData().getName(), id);
		// if (entity == null)
		// {
		// throw new UnknownEntityException("entity [" + getEntityMetaData().getName() + "] with "
		// + getEntityMetaData().getIdAttribute().getName() + " [" + getIdValue().toString()
		// + "] does not exist");
		// }
		// }
		// return entity;
	}
}
