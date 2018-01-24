package org.molgenis.data.validation;

import org.molgenis.util.BatchErrorsException;
import org.springframework.validation.AbstractErrors;

import java.util.List;

@SuppressWarnings({ "squid:S2166" })
public class BatchValidationException extends BatchErrorsException
{
	public BatchValidationException(List<AbstractErrors> abstractErrors)
	{
		this(abstractErrors, null);
	}

	BatchValidationException(List<AbstractErrors> abstractErrors, Throwable cause)
	{
		super(abstractErrors, cause);
	}
}
