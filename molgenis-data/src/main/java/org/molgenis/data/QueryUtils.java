package org.molgenis.data;

import org.molgenis.data.QueryRule.Operator;

public class QueryUtils
{
	public static boolean containsOperator(Query q, Operator operator)
	{
		boolean searchOperator = q.getRules().stream().anyMatch(e -> {
			return e.getOperator() == operator;
		});
		return searchOperator;
	}
}
