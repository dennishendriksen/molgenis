package org.molgenis.bootstrap;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import java.io.File;

@SuppressWarnings("unused")
@Component
public class BootstrapperApplicationListener implements ApplicationListener<ContextRefreshedEvent>, PriorityOrdered
{
	private final MolgenisBootstrapper molgenisBootstrapper;

	public BootstrapperApplicationListener(MolgenisBootstrapper molgenisBootstrapper)
	{
		this.molgenisBootstrapper = molgenisBootstrapper;
	}

	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (new File(System.getProperty("molgenis.home") + File.separator + "molgenis-server.properties").exists())
		{
			molgenisBootstrapper.bootstrap(event);
		}
	}

	@Override
	public int getOrder()
	{
		return PriorityOrdered.HIGHEST_PRECEDENCE; // bootstrap application before doing anything else
	}
}
