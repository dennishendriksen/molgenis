package org.molgenis.js.magma

import com.google.common.base.Stopwatch
import com.google.common.collect.Maps
import org.molgenis.data.Entity
import org.molgenis.data.meta.AttributeType
import org.molgenis.data.meta.model.Attribute
import org.molgenis.js.nashorn.NashornScriptEngine
import org.molgenis.script.ScriptException
import org.molgenis.util.UnexpectedEnumException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.String.format
import java.time.ZoneId
import java.util.Objects.requireNonNull
import java.util.concurrent.TimeUnit.MICROSECONDS
import java.util.stream.Collectors.toList
import java.util.stream.StreamSupport.stream

/**
 * JavaScript script evaluator using the Nashorn script engine.
 */
@Component
class JsMagmaScriptEvaluator(jsScriptEngine: NashornScriptEngine) {

    private val jsScriptEngine: NashornScriptEngine

    init {
        this.jsScriptEngine = requireNonNull(jsScriptEngine)
    }

    /**
     * Evaluate a expression for the given entity.
     *
     * @param expression JavaScript expression
     * @param entity     entity
     * @return evaluated expression result, return type depends on the expression.
     */
    fun eval(expression: String, entity: Entity): Any? {
        var stopwatch: Stopwatch? = null
        if (LOG.isTraceEnabled()) {
            stopwatch = Stopwatch.createStarted()
        }

        val scriptEngineValueMap = toScriptEngineValueMap(entity)
        val value: Any?
        try {
            value = jsScriptEngine.invokeFunction("evalScript", expression, scriptEngineValueMap)
        } catch (t: Throwable) {
            return ScriptException(t)
        }

        if (stopwatch != null) {
            stopwatch.stop()
            LOG.trace("Script evaluation took {} µs", stopwatch.elapsed(MICROSECONDS))
        }

        return value
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(JsMagmaScriptEvaluator::class.java!!)

        private fun toScriptEngineValueMap(entity: Entity): Map<String, Any> {
            val map = Maps.newHashMap<String, Any>()
            entity.entityType
                    .atomicAttributes
                    .forEach { attr -> map.put(attr.name, toScriptEngineValue(entity, attr)) }
            return map
        }

        private fun toScriptEngineValue(entity: Entity?, attr: Attribute): Any? {
            var value: Any? = null

            val attrName = attr.name
            val attrType = attr.dataType
            when (attrType) {
                AttributeType.BOOL -> value = entity!!.getBoolean(attrName)
                AttributeType.CATEGORICAL, AttributeType.FILE, AttributeType.XREF -> {
                    val xrefEntity = entity!!.getEntity(attrName)
                    value = if (xrefEntity != null)
                        toScriptEngineValue(xrefEntity,
                                xrefEntity.entityType.idAttribute!!)
                    else
                        null
                }
                AttributeType.CATEGORICAL_MREF, AttributeType.MREF, AttributeType.ONE_TO_MANY -> {
                    val mrefEntities = entity!!.getEntities(attrName)
                    value = stream(mrefEntities.spliterator(), false).map<Any> { mrefEntity -> toScriptEngineValue(mrefEntity, mrefEntity.entityType.idAttribute!!) }
                            .collect<List<Any>, Any>(toList())
                }
                AttributeType.DATE -> {
                    val localDate = entity!!.getLocalDate(attrName)
                    if (localDate != null) {
                        value = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }
                }
                AttributeType.DATE_TIME -> {
                    val instant = entity!!.getInstant(attrName)
                    if (instant != null) {
                        value = instant.toEpochMilli()
                    }
                }
                AttributeType.DECIMAL -> value = entity!!.getDouble(attrName)
                AttributeType.EMAIL, AttributeType.ENUM, AttributeType.HTML, AttributeType.HYPERLINK, AttributeType.SCRIPT, AttributeType.STRING, AttributeType.TEXT -> value = entity!!.getString(attrName)
                AttributeType.INT -> value = entity!!.getInt(attrName)
                AttributeType.LONG -> value = entity!!.getLong(attrName)
                AttributeType.COMPOUND -> throw RuntimeException(format("Illegal attribute type [%s]", attrType.toString()))
                else -> throw UnexpectedEnumException(attrType)
            }
            return value
        }
    }
}
