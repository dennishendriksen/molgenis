package org.molgenis.data.mongo;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Register the JpaRepositories by the DataService
 */
@Component
public class MongoRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	@Override
	public int getOrder()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// TODO Auto-generated method stub

	}
}
