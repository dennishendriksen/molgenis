package org.molgenis.r

import org.molgenis.script.Script
import org.molgenis.script.ScriptRunner
import org.molgenis.script.ScriptUtils
import org.springframework.stereotype.Service

import java.lang.String.format
import java.util.Objects.requireNonNull

@Service
class RScriptRunner(rScriptExecutor: RScriptExecutor) : ScriptRunner {

    private val rScriptExecutor: RScriptExecutor

    override val name: String
        get() = NAME

    init {
        this.rScriptExecutor = requireNonNull(rScriptExecutor)
    }

    override fun runScript(script: Script, parameters: Map<String, Any>): String? {
        val rScript = ScriptUtils.generateScript(script, parameters)
        val outputFile = getOutputFile(parameters)
        return rScriptExecutor.executeScript(rScript, outputFile)
    }

    private fun getOutputFile(parameters: Map<String, Any>): String? {
        val outputFile = parameters["outputFile"] ?: return null
        if (outputFile !is String) {
            throw RuntimeException(format("Parameter outputFile is of type '%s' instead of '%s'",
                    outputFile.javaClass.getSimpleName(), String::class.java!!.getSimpleName()))
        }
        return outputFile
    }

    companion object {
        private val NAME = "R"
    }
}
