package org.molgenis.data.validation;

import com.google.common.collect.ImmutableList;
import org.springframework.validation.AbstractErrors;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "squid:S2166" })
public class ValidationException extends BatchValidationException
{
	public ValidationException(AbstractErrors errors)
	{
		this(requireNonNull(errors), null);
	}

	private ValidationException(AbstractErrors errors, Throwable cause)
	{
		super(ImmutableList.of(requireNonNull(errors)), cause);
	}
}
