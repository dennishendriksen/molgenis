package org.molgenis.util;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * {@link Serializable} version of {@link MessageSourceResolvable} that supports nested errors.
 */
public class Error implements Serializable
{
	private final DefaultMessageSourceResolvable messageSourceResolvable;
	private final List<Error> childErrors;

	public Error(DefaultMessageSourceResolvable messageSourceResolvable)
	{
		this(messageSourceResolvable, emptyList());
	}

	public Error(DefaultMessageSourceResolvable messageSourceResolvable, List<Error> childErrors)
	{
		this.messageSourceResolvable = requireNonNull(messageSourceResolvable);
		this.childErrors = requireNonNull(childErrors);
	}

	public String getCode()
	{
		return stream(messageSourceResolvable.getCodes()).collect(joining(","));
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
