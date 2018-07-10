package org.molgenis.data.importer;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.Tables;

import javax.annotation.Nullable;

public interface MetaDataParser
{
	/**
	 * Parses the metadata of the entities to import.
	 *
	 * @param tables    {@link RepositoryCollection} containing the data to parse
	 * @param packageId , the package where the entities should go. Default if none was supplied
	 * @return {@link ParsedMetaData}
	 */
	ParsedMetaData parse(Tables tables, @Nullable String packageId);

	/**
	 * Generates a {@link EntitiesValidationReport} by parsing all data from a supplied source
	 */
	EntitiesValidationReport validate(RepositoryCollection source);
}