package org.molgenis.data.elasticsearch.logback;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class LoggingEventMetaData extends DefaultEntityMetaData
{
	public static final LoggingEventMetaData INSTANCE = new LoggingEventMetaData();

	public static final String ENTITY_NAME = "LoggingEvent";

	public static final AttributeMetaData IDENTIFIER;
	public static final AttributeMetaData TIMESTAMP;
	public static final AttributeMetaData THREAD;
	public static final AttributeMetaData LEVEL;
	public static final AttributeMetaData LOGGER;
	public static final AttributeMetaData MESSAGE;
	public static final AttributeMetaData STACKTRACE;

	static
	{
		IDENTIFIER = attribute("identifier").setIdAttribute(true).setNillable(false).setVisible(false);
		TIMESTAMP = attribute("timestamp").setDataType(MolgenisFieldTypes.DATETIME);
		THREAD = attribute("thread");
		LEVEL = attribute("level");
		LOGGER = attribute("logger");
		MESSAGE = attribute("message").setDataType(MolgenisFieldTypes.TEXT);
		STACKTRACE = attribute("stacktrace").setDataType(MolgenisFieldTypes.TEXT);
	}

	private LoggingEventMetaData()
	{
		super(ENTITY_NAME);
		setBackend(ElasticsearchRepositoryCollection.NAME);
		addAttributeMetaData(IDENTIFIER).addAttributeMetaData(TIMESTAMP).addAttributeMetaData(THREAD)
				.addAttributeMetaData(LEVEL).addAttributeMetaData(LOGGER).addAttributeMetaData(MESSAGE)
				.addAttributeMetaData(STACKTRACE);
	}
}
