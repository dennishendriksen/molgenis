package org.molgenis.data.elasticsearch.generator;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class QueryGenerationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public QueryGenerationException()
	{
	}

	public QueryGenerationException(String msg)
	{
		super(msg);
	}

	public QueryGenerationException(Throwable t)
	{
		super(t);
	}

	public QueryGenerationException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
