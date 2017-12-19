package org.molgenis.js

import org.molgenis.script.Script
import org.molgenis.script.ScriptRunner
import org.molgenis.script.ScriptUtils
import org.springframework.stereotype.Service

import java.util.Objects.requireNonNull

/**
 * Runs a JavaScript with the given inputs and returns one output
 */
@Service
class JsScriptRunner(jsScriptExecutor: JsScriptExecutor) : ScriptRunner {

    private val jsScriptExecutor: JsScriptExecutor

    override val name: String
        get() = NAME

    init {
        this.jsScriptExecutor = requireNonNull(jsScriptExecutor)
    }

    override fun runScript(script: Script, parameters: Map<String, Any>): String? {
        val jsScript = ScriptUtils.generateScript(script, parameters)
        val scriptResult = jsScriptExecutor.executeScript(jsScript)
        return scriptResult?.toString()
    }

    companion object {
        private val NAME = "JavaScript"
    }
}
