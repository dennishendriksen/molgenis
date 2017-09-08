package org.molgenis.data.rest.v3.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class ReadEntitiesRequest
{
	@Min(value = 0, message = "test")
	private Long start;

	@Max(1000)
	private Long num;

	public Long getStart()
	{
		return start;
	}

	public void setStart(Long start)
	{
		this.start = start;
	}

	public Long getNum()
	{
		return num;
	}

	public void setNum(Long num)
	{
		this.num = num;
	}
}
