package org.molgenis.js.magma

import org.molgenis.script.Script
import org.molgenis.script.ScriptRunner
import org.springframework.stereotype.Service

import java.util.Objects.requireNonNull

/**
 * Runs a JavaScript using the Magma API with the given inputs and returns one output
 */
@Service
class JsMagmaScriptRunner(jsMagmaScriptExecutor: JsMagmaScriptExecutor) : ScriptRunner {

    private val jsScriptExecutor: JsMagmaScriptExecutor

    override val name: String
        get() = NAME

    init {
        this.jsScriptExecutor = requireNonNull(jsMagmaScriptExecutor)
    }

    override fun runScript(script: Script, parameters: Map<String, Any>): String? {
        val jsScript = script.content
        val scriptResult = jsScriptExecutor.executeScript(jsScript, parameters)
        return scriptResult?.toString()
    }

    companion object {
        val NAME = "JavaScript (Magma)"
    }
}
