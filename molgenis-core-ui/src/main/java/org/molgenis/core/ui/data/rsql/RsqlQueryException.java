package org.molgenis.core.ui.data.rsql;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class RsqlQueryException extends RuntimeException
{
	public RsqlQueryException(String msg)
	{
		super(msg);
	}
}
