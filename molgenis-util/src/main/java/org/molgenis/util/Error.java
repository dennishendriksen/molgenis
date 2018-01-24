package org.molgenis.util;

import org.springframework.context.MessageSourceResolvable;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * {@link Serializable} version of {@link MessageSourceResolvable} that supports nested errors.
 */
public class Error implements Serializable
{
	private final transient MessageSourceResolvable messageSourceResolvable;
	private final List<Error> childErrors;

	public Error(MessageSourceResolvable messageSourceResolvable)
	{
		this(messageSourceResolvable, emptyList());
	}

	public Error(MessageSourceResolvable messageSourceResolvable, List<Error> childErrors)
	{
		this.messageSourceResolvable = requireNonNull(messageSourceResolvable);
		this.childErrors = requireNonNull(childErrors);
	}

	public MessageSourceResolvable getMessageSourceResolvable()
	{
		return messageSourceResolvable;
	}

	public Stream<Error> getChildren()
	{
		return childErrors.stream();
	}
}
