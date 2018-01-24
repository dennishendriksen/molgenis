package org.molgenis.util;

import org.springframework.validation.AbstractErrors;
import org.springframework.validation.Errors;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Base class for exceptions exposing {@link Errors} for multiple objects.
 *
 * @see ErrorsException
 */
public class BatchErrorsException extends RuntimeException
{
	// require AbstractErrors instead of Errors because AbstractErrors is Serializable
	private final List<AbstractErrors> errorsCollection;

	protected BatchErrorsException(List<AbstractErrors> errorsCollection)
	{
		this(errorsCollection, null);
	}

	protected BatchErrorsException(List<AbstractErrors> errorsCollection, Throwable cause)
	{
		super(errorsCollection.stream().map(Errors::toString).collect(joining(",")), cause);
		this.errorsCollection = requireNonNull(errorsCollection);
	}

	public List<Errors> getErrorsList()
	{
		return unmodifiableList(errorsCollection);
	}
}
