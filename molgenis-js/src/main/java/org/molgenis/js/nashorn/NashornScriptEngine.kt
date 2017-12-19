package org.molgenis.js.nashorn

import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import jdk.nashorn.api.scripting.ScriptObjectMirror
import org.molgenis.util.ResourceUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.Arrays.asList
import javax.script.*

@Component
class NashornScriptEngine {

    private var scriptEngine: ScriptEngine? = null
    private var bindingsThreadLocal: ThreadLocal<Bindings>? = null

    init {
        initScriptEngine()
    }

    fun invokeFunction(functionName: String, vararg args: Any): Any? {
        val bindings = bindingsThreadLocal!!.get()
        val returnValue = (bindings[functionName] as JSObject).call(this, *args)
        return convertNashornValue(returnValue)
    }

    @Throws(ScriptException::class)
    fun eval(script: String): Any {
        val bindings = bindingsThreadLocal!!.get()
        return scriptEngine!!.eval(script, bindings)
    }

    private fun initScriptEngine() {
        LOG.debug("Initializing Nashorn script engine ...")
        val factory = NashornScriptEngineFactory()
        scriptEngine = factory.getScriptEngine { s -> false } // create engine with class filter exposing no classes

        // construct common JavaScript content string from defined resources
        val commonJs = StringBuilder(1000000)
        RESOURCE_NAMES.forEach { resourceName ->
            try {
                commonJs.append(ResourceUtils.getString(javaClass, resourceName)).append('\n')
            } catch (e: IOException) {
                throw RuntimeException("", e)
            }
        }

        // pre-compile common JavaScript
        val compiledScript: CompiledScript
        try {
            compiledScript = (scriptEngine as Compilable).compile(commonJs.toString())
        } catch (e: ScriptException) {
            throw RuntimeException("", e)
        }

        // create bindings per thread resulting in a JavaScript global per thread
        bindingsThreadLocal = ThreadLocal.withInitial {
            val bindings = scriptEngine!!.createBindings()
            try {
                // evaluate pre-compiled common JavaScript
                compiledScript.eval(bindings)
            } catch (e: ScriptException) {
                throw RuntimeException("", e)
            }

            bindings
        }

        LOG.debug("Initialized Nashorn script engine")
    }

    private fun convertNashornValue(nashornValue: Any?): Any? {
        if (nashornValue == null) {
            return null
        }

        val convertedValue: Any
        if (nashornValue is ScriptObjectMirror) {
            val scriptObjectMirror = nashornValue as ScriptObjectMirror?
            if (scriptObjectMirror!!.isArray) {
                convertedValue = scriptObjectMirror.values
            } else {
                if ("Date" == scriptObjectMirror.className) {
                    // convert to Java Interface
                    val jsDate = (scriptEngine as Invocable).getInterface<JsDate>(scriptObjectMirror, JsDate::class.java)
                    return jsDate.time
                } else {
                    throw RuntimeException("Unable to convert [ScriptObjectMirror]")
                }
            }
        } else {
            convertedValue = nashornValue
        }
        return convertedValue
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NashornScriptEngine::class.java!!)

        private val RESOURCE_NAMES: List<String>

        init {
            RESOURCE_NAMES = asList("/js/es6-shims.js", "/js/math.min.js", "/js/script-evaluator.js")
        }
    }
}
