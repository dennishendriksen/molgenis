package org.molgenis.data.validation;

import org.molgenis.util.Error;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.AbstractErrors;

import java.util.List;
import java.util.stream.Stream;

public class ValidationError extends Error
{
	private final List<AbstractErrors> errors;

	private ValidationError(List<AbstractErrors> errors)
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