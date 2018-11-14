package org.molgenis.jobs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.i18n.UserLocaleResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JobExecutorLocaleServiceImplTest extends AbstractMockitoTest {
  @Mock private UserLocaleResolver userLocaleResolver;
  private JobExecutorLocaleServiceImpl jobExecutorLocaleServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    jobExecutorLocaleServiceImpl = new JobExecutorLocaleServiceImpl(userLocaleResolver);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testJobExecutorLocaleServiceImpl() {
    new JobExecutorLocaleServiceImpl(null);
  }

  @Test
  public void testCreateLocale() {
    String username = "MyUsername";
    JobExecution jobExecution =
        when(mock(JobExecution.class).getUser()).thenReturn(username).getMock();
    Locale locale = Locale.getDefault();
    when(userLocaleResolver.resolveLocale(username)).thenReturn(locale);
    assertEquals(jobExecutorLocaleServiceImpl.createLocale(jobExecution), locale);
  }
}
