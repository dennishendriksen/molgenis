package org.molgenis.data.elasticsearch.generator;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class QueryGenerationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	QueryGenerationException(String msg)
	{
		super(msg);
	}
}
