package org.molgenis.web.bootstrap;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.*;
import java.util.Properties;

import static java.util.Objects.requireNonNull;
import static org.molgenis.web.bootstrap.InitialConfigurationController.URI;

@Controller
@RequestMapping(URI)
public class InitialConfigurationController
{
	public static final String URI = "/initial-configuration";

	private final ApplicationContext applicationContext;

	InitialConfigurationController(ApplicationContext applicationContext)
	{
		this.applicationContext = requireNonNull(applicationContext);
	}

	@GetMapping
	public String init()
	{
		return "view-initial-configuration";
	}

	@PostMapping
	public void init(@ModelAttribute ConfigurationRequest initializationRequest)
	{
		writeProperties(initializationRequest);

		if (applicationContext instanceof AnnotationConfigWebApplicationContext)
		{
			AnnotationConfigWebApplicationContext context = (AnnotationConfigWebApplicationContext) applicationContext;
			context.getServletContext().setAttribute("configurationCompleted", true);
			context.close();
		}
		else
		{
			throw new IllegalArgumentException("'applicationContext' is not a AnnotationConfigWebApplicationContext");
		}
	}

	private void writeProperties(ConfigurationRequest configurationRequest)
	{
		Properties properties = new Properties();
		properties.setProperty("db_uri", String.format("jdbc:postgresql://%s:%s/%s", configurationRequest.getHostname(),
				configurationRequest.getPort(), configurationRequest.getDatabase()));
		properties.setProperty("db_user", configurationRequest.getUsername());
		properties.setProperty("db_password", configurationRequest.getPassword());
		properties.setProperty("admin.password", configurationRequest.getAdminPassword());
		properties.setProperty("admin.email", configurationRequest.getAdminEmail());
		properties.setProperty("elasticsearch.cluster.name", configurationRequest.getClusterName());
		properties.setProperty("elasticsearch.transport.addresses", configurationRequest.getTransportAddresses());

		// get molgenis home directory
		String molgenisHomeDir = System.getProperty("molgenis.home");
		File propertiesFile = new File(molgenisHomeDir, "molgenis-server.properties");

		try (OutputStream out = new FileOutputStream(propertiesFile))
		{
			properties.store(out, "Molgenis server properties");
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
