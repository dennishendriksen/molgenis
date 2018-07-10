package org.molgenis.data.importer.emx;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Tables;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ParsedMetaData;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Parameter object for the import job.
 */
public class EmxImportJob
{
	final DatabaseAction dbAction;

	// TODO: there is some overlap between source and parsedMetaData
	public final Tables tables;
	final ParsedMetaData parsedMetaData;

	public final EntityImportReport report = new EntityImportReport();
	private final String packageId;

	EmxImportJob(DatabaseAction dbAction, Tables tables, ParsedMetaData parsedMetaData,
			@Nullable String packageId)
	{
		this.dbAction = dbAction;
		this.tables = tables;
		this.parsedMetaData = parsedMetaData;
		this.packageId = packageId;
	}

	Tables getSource()
	{
		return tables;
	}

	ParsedMetaData getParsedMetaData()
	{
		return parsedMetaData;
	}

	public Optional<String> getPackageId()
	{
		return packageId != null ? Optional.of(packageId) : Optional.empty();
	}
}