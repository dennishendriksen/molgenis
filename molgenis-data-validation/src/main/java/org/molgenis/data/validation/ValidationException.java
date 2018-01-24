package org.molgenis.data.validation;

import org.molgenis.util.Error;
import org.molgenis.util.ErrorException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "squid:S2166" })
public class ValidationException extends ErrorException
{
	public ValidationException(Errors errors)
	{
		this(errors, null);
	}

	public ValidationException(Errors errors, Throwable cause)
	{
		this(singletonList(requireNonNull(errors)), cause);
	}

	public ValidationException(List<Errors> errors)
	{
		this(errors, null);
	}

	public ValidationException(List<Errors> errors, Throwable cause)
	{
		super(new ValidationError(errors), cause);
	}

	private static class ValidationError extends Error
	{
		private final List<Errors> errors;

		private ValidationError(List<Errors> errors)
		{
			super(new DefaultMessageSourceResolvable("V00"));
			this.errors = errors;
		}

		@Override
		public Stream<Error> getChildren()
		{
			return errors.stream().flatMap(error -> error.getAllErrors().stream()).map(Error::new);
		}
	}
}
