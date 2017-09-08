package org.molgenis.data.rest.v3.model;

import java.util.List;

public class CreateEntitiesRequest
{
	private List<CreateEntityRequest> entities;

	public List<CreateEntityRequest> getEntities()
	{
		return entities;
	}

	public void setEntities(List<CreateEntityRequest> entities)
	{
		this.entities = entities;
	}
}
