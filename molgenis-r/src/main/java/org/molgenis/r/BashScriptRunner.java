package org.molgenis.r;

import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptRunner;
import org.molgenis.script.core.ScriptUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Service
public class BashScriptRunner implements ScriptRunner
{
	private static final String NAME = "Bash";

	private final BashScriptExecutor bashScriptExecutor;

	public BashScriptRunner(BashScriptExecutor bashScriptExecutor)
	{
		this.bashScriptExecutor = requireNonNull(bashScriptExecutor);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
		String bashScript = ScriptUtils.generateScript(script, parameters);
		String outputFile = getOutputFile(parameters);
		return bashScriptExecutor.executeScript(bashScript, outputFile);
	}

	private String getOutputFile(Map<String, Object> parameters)
	{
		Object outputFile = parameters.get("outputFile");
		if (outputFile == null)
		{
			return null;
		}
		if (!(outputFile instanceof String))
		{
			throw new RuntimeException(format("Parameter outputFile is of type '%s' instead of '%s'",
					outputFile.getClass().getSimpleName(), String.class.getSimpleName()));
		}
		return (String) outputFile;
	}
}
