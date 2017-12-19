package org.molgenis.js

import org.molgenis.js.nashorn.NashornScriptEngine
import org.springframework.stereotype.Service
import java.util.Objects.requireNonNull
import javax.script.ScriptException

/**
 * Executes a JavaScript
 */
@Service
internal class JsScriptExecutor(jsScriptEngine: NashornScriptEngine) {
    private val jsScriptEngine: NashornScriptEngine

    init {
        this.jsScriptEngine = requireNonNull(jsScriptEngine)
    }

    /**
     * Executes the given JavaScript, e.g. 'var product = 2 * 3; return product;'
     *
     * @param jsScript JavaScript
     * @return value of which the type depends on the JavaScript type of the returned variable
     */
    fun executeScript(jsScript: String): Any {
        val jsScriptWithFunction = "(function (){$jsScript})();"
        try {
            return jsScriptEngine.eval(jsScriptWithFunction)
        } catch (e: ScriptException) {
            throw org.molgenis.script.ScriptException(e)
        }

    }
}
