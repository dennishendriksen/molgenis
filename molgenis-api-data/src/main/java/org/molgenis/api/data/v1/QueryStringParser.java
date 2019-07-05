package org.molgenis.api.data.v1;

import java.util.Map;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.web.rsql.MolgenisRSQL;

/**
 * Creates a Query object from a http request. Used by the RestController method that returns csv.
 *
 * <p>Parameters:
 *
 * <p>q: the query
 *
 * <p>attributes: the attributes to return, if not specified returns all attributes
 *
 * <p>start: the index of the first row, default 0
 *
 * <p>num: the number of results to return, default 100, max 100000
 *
 * <p>
 *
 * <p>Example: /api/v1/csv/person?q=firstName==Piet&attributes=firstName,lastName&start=10&num=100
 */
public class QueryStringParser {
  private final Repository<Entity> repository;
  private final MolgenisRSQL molgenisRSQL;

  public QueryStringParser(Repository<Entity> repository, MolgenisRSQL molgenisRSQL) {
    this.repository = repository;
    this.molgenisRSQL = molgenisRSQL;
  }

  public Query<Entity> parseQueryString(Map<String, String[]> parameterMap) {
    QueryImpl<Entity> q = new QueryImpl<>();

    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
      String paramName = entry.getKey();
      String[] paramValues = entry.getValue();

      if ((paramValues != null) && (paramValues.length > 0) && (paramValues[0] != null)) {
        if (paramName.equalsIgnoreCase("num")) {
          q.pageSize(DataConverter.toInt(paramValues[0]));
        } else if (paramName.equalsIgnoreCase("start")) {
          q.offset(DataConverter.toInt(paramValues[0]));
        } else if (paramName.equalsIgnoreCase("q")) {
          Query<Entity> query = molgenisRSQL.createQuery(paramValues[0], repository);
          for (QueryRule rule : query.getRules()) {
            q.addRule(rule);
          }
        }
      }
    }

    return q;
  }
}
