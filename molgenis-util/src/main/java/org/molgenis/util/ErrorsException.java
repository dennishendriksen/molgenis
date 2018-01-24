package org.molgenis.util;

import org.springframework.validation.AbstractErrors;
import org.springframework.validation.Errors;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * Base class for exceptions exposing {@link Errors} for a single object.
 *
 * @see BatchErrorsException
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public abstract class ErrorsException extends BatchErrorsException
{
	protected ErrorsException(AbstractErrors errors)
	{
		this(errors, null);
	}

	protected ErrorsException(AbstractErrors errors, Throwable cause)
	{
		super(singletonList(requireNonNull(errors)), cause);
	}

	public Errors getErrors()
	{
		return getErrorsList().iterator().next();
	}
}
