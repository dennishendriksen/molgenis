package org.molgenis.data.rest.v3.model;

import java.util.List;

public class DeleteEntitiesRequest
{
	private List<String> ids;

	public List<String> getIds()
	{
		return ids;
	}

	public void setIds(List<String> ids)
	{
		this.ids = ids;
	}
}
