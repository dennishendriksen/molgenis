package org.molgenis.data.settings;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for application and plugin settings entities. Settings are read/written from/to data source.
 */
public abstract class DefaultSettingsEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final String entityName;

	@Autowired
	private DataService dataService;

	private transient Entity cachedEntity;

	public DefaultSettingsEntity(String entityId)
	{
		this.entityName = SettingsEntityMeta.PACKAGE_NAME + '_' + entityId;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return RunAsSystemProxy.runAsSystem(() -> {
			return dataService.getEntityMetaData(entityName);
		});
	}

	@Override
	public Object getIdValue()
	{
		return getEntity().getIdValue();
	}

	@Override
	public Object get(AttributeMetaData attr)
	{
		return getEntity().get(attr);
	}

	@Override
	public String getString(AttributeMetaData attr)
	{
		return getEntity().getString(attr);
	}

	@Override
	public Integer getInt(AttributeMetaData attr)
	{
		return getEntity().getInt(attr);
	}

	@Override
	public Long getLong(AttributeMetaData attr)
	{
		return getEntity().getLong(attr);
	}

	@Override
	public Boolean getBoolean(AttributeMetaData attr)
	{
		return getEntity().getBoolean(attr);
	}

	@Override
	public Double getDouble(AttributeMetaData attr)
	{
		return getEntity().getDouble(attr);
	}

	@Override
	public java.util.Date getUtilDate(AttributeMetaData attr)
	{
		return getEntity().getUtilDate(attr);
	}

	@Override
	public Entity getEntity(AttributeMetaData attr)
	{
		return getEntity().getEntity(attr);
	}

	@Override
	public <E extends Entity> E getEntity(AttributeMetaData attr, Class<E> clazz)
	{
		return getEntity().getEntity(attr, clazz);
	}

	@Override
	public EntityCollection getEntities(AttributeMetaData attr)
	{
		return getEntity().getEntities(attr);
	}

	@Override
	public void set(AttributeMetaData attr, Object value)
	{
		Entity entity = getEntity();
		entity.set(attr, value);
		updateEntity(entity);
	}

	@Override
	public void set(Entity values)
	{
		Entity entity = getEntity();
		entity.set(values);
		updateEntity(entity);
	}

	/**
	 * Adds a listener for this settings entity that fires on entity updates
	 * 
	 * @param settingsEntityListener
	 *            listener for this settings entity
	 */
	public void addListener(SettingsEntityListener settingsEntityListener)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.addEntityListener(entityName, new EntityListener()
			{
				@Override
				public void postUpdate(Entity entity)
				{
					settingsEntityListener.postUpdate(entity);
				}

				@Override
				public Object getEntityId()
				{
					return getEntityMetaData().getSimpleName();
				}
			});
		});
	}

	/**
	 * Removes a listener for this settings entity that fires on entity updates
	 * 
	 * @param settingsEntityListener
	 *            listener for this settings entity
	 */
	public void removeListener(SettingsEntityListener settingsEntityListener)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.removeEntityListener(entityName, new EntityListener()
			{

				@Override
				public void postUpdate(Entity entity)
				{
					settingsEntityListener.postUpdate(entity);
				}

				@Override
				public Object getEntityId()
				{
					return getEntityMetaData().getSimpleName();
				}
			});
		});
	}

	private Entity getEntity()
	{
		if (cachedEntity == null)
		{
			String id = getEntityMetaData().getSimpleName();
			cachedEntity = RunAsSystemProxy.runAsSystem(() -> {
				Entity entity = dataService.findOne(entityName, id);

				// refresh cache on settings update
				dataService.addEntityListener(entityName, new EntityListener()
				{
					@Override
					public void postUpdate(Entity entity)
					{
						cachedEntity = entity;
					}

					@Override
					public Object getEntityId()
					{
						return id;
					}
				});
				return entity;
			});

		}
		return cachedEntity;
	}

	private void updateEntity(Entity entity)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.update(entityName, entity);

			// cache refresh is handled via entity listener
			return null;
		});
	}
}
