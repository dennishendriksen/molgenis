package org.molgenis.api.permissions.exceptions.rsql;

import static org.testng.Assert.assertEquals;

import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownPermissionQueryParamExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownPermissionQueryParamException("type"), lang, message);
  }

  @Test
  public void testGetMessage() {
    CodedRuntimeException ex = new UnknownPermissionQueryParamException("type");
    assertEquals(ex.getMessage(), "key:type");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unknown field 'type' in query, only 'user' and 'role' are supported."}
    };
  }
}
