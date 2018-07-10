package org.molgenis.data.csv;

import au.com.bytecode.opencsv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;

class CsvUtils
{
	private static final char DEFAULT_SEPARATOR = ',';
	private static final char[] SEPARATORS = new char[] { ',', '\t', ';', ' ', '|' };

	private CsvUtils()
	{
	}

	static char determineSeparator(Path path)
	{
		char separator = DEFAULT_SEPARATOR;
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(newInputStream(path), UTF_8)))
		{
			String line = bufferedReader.readLine();
			if (line != null)
			{
				int maxTokens = -1;
				for (char c : SEPARATORS)
				{
					String[] tokens = new CSVParser(c).parseLine(line);
					if (tokens.length > maxTokens)
					{
						separator = c;
						maxTokens = tokens.length;
					}
				}

			}
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return separator;
	}
}
