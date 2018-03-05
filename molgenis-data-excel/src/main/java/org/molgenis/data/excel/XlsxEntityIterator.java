package org.molgenis.data.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.molgenis.data.Entity;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;

public class XlsxEntityIterator implements Iterator<Entity>, Closeable
{
	private final OPCPackage opcPackage;
	private final String sheetName;
	private InputSource sheetSource;

	public XlsxEntityIterator(OPCPackage opcPackage, String sheetName)
	{
		this.opcPackage = requireNonNull(opcPackage);
		this.sheetName = requireNonNull(sheetName);
	}

	@Override
	public boolean hasNext()
	{
		if (!initialized)
		{
			init();
		}
		return false; // TODO
	}

	@Override
	public Entity next()
	{
		if (!initialized)
		{
			init();
		}
		return null; // TODO
	}

	public void close()
	{
		try
		{
			opcPackage.close();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private boolean init()
	{
		XSSFReader xssfReader;
		try
		{
			xssfReader = new XSSFReader(opcPackage);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		catch (OpenXML4JException e)
		{
			throw new RuntimeException(e);
		}
		SharedStringsTable sst = null;
		try
		{
			sst = xssfReader.getSharedStringsTable();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		catch (InvalidFormatException e)
		{
			throw new RuntimeException(e);
		}

		try
		{
			XMLReader xmlReader = fetchSheetParser(sst);
		}
		catch (SAXException | ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}

		{
			try
			{
				for (SheetIterator sheetIterator = (SheetIterator) xssfReader.getSheetsData(); sheetIterator.hasNext(); )
				{
					InputStream inputStream = sheetIterator.next();
					if (sheetIterator.getSheetName().equals(sheetName))
					{
						sheetSource = new InputSource(inputStream);
					}
				}
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
			catch (InvalidFormatException e)
			{
				throw new RuntimeException(e);
			}
			if (sheetSource == null)
			{
				throw new RuntimeException("unknown sheet");
			}
		}
	}

	public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException, ParserConfigurationException
	{
		XMLReader parser = SAXHelper.newXMLReader();
		ContentHandler handler = new SheetHandler(sst);
		parser.setContentHandler(handler);
		return parser;
	}

	private class SheetHandler extends DefaultHandler
	{
		public SheetHandler(SharedStringsTable stringsTable)
		{
		}
	}
}
