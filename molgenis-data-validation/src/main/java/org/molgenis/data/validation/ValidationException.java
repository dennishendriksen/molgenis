package org.molgenis.data.validation;

import org.molgenis.util.ErrorException;

public class ValidationException extends ErrorException
{
	public ValidationException(ValidationError validationError)
	{
		super(validationError);
	}
}
