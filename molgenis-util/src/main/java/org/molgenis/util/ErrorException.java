package org.molgenis.util;

import org.springframework.context.MessageSourceResolvable;

/**
 * Exception containing {@link MessageSourceResolvable} error.
 */
public class ErrorException extends RuntimeException
{
	private final Error error;

	protected ErrorException(Error error)
	{
		this(error, null);
	}

	protected ErrorException(Error error, Throwable cause)
	{
		super(null, cause);
		this.error = error;
	}

	public Error getError()
	{
		return error;
	}

	@Override
	public String getMessage()
	{
		return error.getMessageSourceResolvable().getDefaultMessage(); // TODO print tree?
	}
}
