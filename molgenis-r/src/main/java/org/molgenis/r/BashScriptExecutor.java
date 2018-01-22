package org.molgenis.r;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Executes an R script using OpenCPU
 */
@Service
public class BashScriptExecutor
{

	public String executeScript(String bashScript, String outputFile)
	{
		ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", '"' + bashScript + '"');
		try
		{
			return IOUtils.toString(processBuilder.start().getInputStream());
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
