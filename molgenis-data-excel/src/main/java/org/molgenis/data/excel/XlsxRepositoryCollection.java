package org.molgenis.data.excel;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.meta.model.EntityType;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class XlsxRepositoryCollection extends FileRepositoryCollection
{
	private static final String NAME = "Microsoft Excel Open XML Format Spreadsheet";
	private static final Set<String> XLSX_EXTENSIONS = Collections.singleton("xlsx");

	private final File file;

	public XlsxRepositoryCollection(File file)
	{
		super(XLSX_EXTENSIONS);
		this.file = requireNonNull(file);
	}

	@Override
	public void init()
	{
		// noop
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Iterable<String> getEntityTypeIds()
	{
		return getSheetNames();
	}

	@Override
	public Repository<Entity> getRepository(String id)
	{
		return new XlsxRepository(file, id);
	}

	@Override
	public boolean hasRepository(String id)
	{
		return getSheetNames().contains(id);
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return hasRepository(entityType.getId());
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		Set<String> sheetNames = getSheetNames();
		return sheetNames.stream().map(this::getRepository).iterator();
	}

	private Set<String> getSheetNames()
	{
		try (OPCPackage opcPackage = OPCPackage.open(file, PackageAccess.READ))
		{
			XSSFReader xssfReader = new XSSFReader(opcPackage);
			Set<String> sheetNames = new LinkedHashSet<>();
			for (SheetIterator sheetIterator = (SheetIterator) xssfReader.getSheetsData(); sheetIterator.hasNext(); )
			{
				sheetIterator.next();
				sheetNames.add(sheetIterator.getSheetName());
			}
			return sheetNames;
		}
		catch (OpenXML4JException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
