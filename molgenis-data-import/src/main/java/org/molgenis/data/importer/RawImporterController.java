package org.molgenis.data.importer;

import static org.molgenis.data.importer.RawImporterController.URI;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Part;

import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.bytecode.opencsv.CSVReader;

@Controller
@RequestMapping(URI)
public class RawImporterController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(RawImporterController.class);

	public static final String ID = "rawimporter";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DataService dataService;

	@Autowired
	public RawImporterController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init()
	{
		return "view-rawimporter";
	}

	@RequestMapping(value = "analyze", method = RequestMethod.POST)
	@ResponseBody
	public FileImportAnalysisResponse analyzeFile(@RequestParam(value = "file") Part file) throws IOException
	{
		CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream(), Charset.forName("UTF-8")));
		try
		{
			String[] headers = csvReader.readNext();
			return new FileImportAnalysisResponse(Arrays.asList(headers));
		}
		finally
		{
			csvReader.close();
		}
	}

	private static class FileImportAnalysisResponse
	{
		private final List<String> headers;

		public FileImportAnalysisResponse(List<String> headers)
		{
			this.headers = headers;
		}

		@SuppressWarnings("unused")
		public List<String> getHeaders()
		{
			return headers;
		}
	}
}
