package org.molgenis.js.magma

import org.molgenis.data.meta.model.AttributeFactory
import org.molgenis.data.meta.model.EntityTypeFactory
import org.molgenis.data.support.DynamicEntity
import org.springframework.stereotype.Service
import java.util.Objects.requireNonNull

/**
 * Executes a JavaScript using the Magma API
 */
@Service
class JsMagmaScriptExecutor(private val jsMagmaScriptEvaluator: JsMagmaScriptEvaluator, entityTypeFactory: EntityTypeFactory,
                            attributeFactory: AttributeFactory) {
    private val entityTypeFactory: EntityTypeFactory
    private val attributeFactory: AttributeFactory

    init {
        this.entityTypeFactory = requireNonNull(entityTypeFactory)
        this.attributeFactory = requireNonNull(attributeFactory)
    }

    /**
     * Execute a JavaScript using the Magma API
     */
    internal fun executeScript(jsScript: String, parameters: Map<String, Any>): Any? {
        val entityType = entityTypeFactory.create("entity")
        parameters.keys.forEach { key -> entityType.addAttribute(attributeFactory.create().setName(key)) }
        val entity = DynamicEntity(entityType)
        parameters.forEach { key, value -> entity.set(key, value) }
        return jsMagmaScriptEvaluator.eval(jsScript, entity)
    }
}
