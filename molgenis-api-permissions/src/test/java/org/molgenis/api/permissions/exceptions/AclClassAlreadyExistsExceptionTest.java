package org.molgenis.api.permissions.exceptions;

import static org.testng.Assert.assertEquals;

import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AclClassAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new AclClassAlreadyExistsException("type"), lang, message);
  }

  @Test
  public void testGetMessage() {
    CodedRuntimeException ex = new AclNotFoundException("type");
    assertEquals(ex.getMessage(), "typeId:type");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Acl class 'type' already exists."},
      {"nl", "Acl class 'type' bestaat al."}
    };
  }
}
