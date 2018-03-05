package org.molgenis.data.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractRepository;

import java.io.File;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class XlsxRepository extends AbstractRepository
{
	private final File file;
	private final String sheetName;

	private final Collection<XlsxEntityIterator> xlsxEntityIterators;

	XlsxRepository(File file, String sheetName)
	{
		this.file = requireNonNull(file);
		this.sheetName = requireNonNull(sheetName);
		this.xlsxEntityIterators = new ArrayList<>();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		OPCPackage opcPackage;
		try
		{
			opcPackage = OPCPackage.open(file, PackageAccess.READ);
		}
		catch (InvalidFormatException e)
		{
			throw new RuntimeException(e);
		}
		XlsxEntityIterator xlsxEntityIterator = new XlsxEntityIterator(opcPackage, sheetName);
		xlsxEntityIterators.add(xlsxEntityIterator);
		return xlsxEntityIterator;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public EntityType getEntityType()
	{
		return null;
	}

	@Override
	public void close()
	{
		xlsxEntityIterators.forEach(XlsxEntityIterator::close);
	}
}
