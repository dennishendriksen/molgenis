package org.molgenis.data.jobs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.settings.mail.MailSettingsImpl;
import org.molgenis.util.mail.JavaMailSenderFactory;
import org.molgenis.util.mail.MailSenderImpl;
import org.molgenis.util.mail.MailSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Import({ JobFactoryRegistry.class, JobExecutionUpdaterImpl.class, JobFactoryRegistrar.class })
public class JobExecutionConfig
{
	@Autowired
	private DataService dataService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private JobExecutionUpdater jobExecutionUpdater;
	@Autowired
	private ExecutorService executorService;
	@Autowired
	private JobFactoryRegistry jobFactoryRegistry;

	@Primary // Use this ExecutorService when no specific bean is demanded
	@Bean
	public ExecutorService executorService()
	{
		return Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("molgenis-job-%d").build());
	}

	@Bean
	public JobExecutor jobExecutor()
	{
		return new JobExecutor(dataService, entityManager, userDetailsService, jobExecutionUpdater, mailSender(),
				executorService, jobFactoryRegistry);
	}

	@Bean
	public JavaMailSenderFactory javaMailSenderFactory()
	{
		return new JavaMailSenderFactory();
	}

	@Bean
	public MailSettings mailSettings()
	{
		return new MailSettingsImpl();
	}

	@Bean

	public MailSender mailSender()
	{
		return new MailSenderImpl(mailSettings(), javaMailSenderFactory());
	}
}
