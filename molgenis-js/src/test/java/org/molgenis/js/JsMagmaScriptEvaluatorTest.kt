package org.molgenis.js

import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.molgenis.data.meta.AttributeType.*
import org.molgenis.data.meta.model.Attribute
import org.molgenis.data.meta.model.EntityType
import org.molgenis.data.support.DynamicEntity
import org.molgenis.js.magma.JsMagmaScriptEvaluator
import org.molgenis.js.nashorn.NashornScriptEngine
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant.now
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.util.*
import java.util.Arrays.asList

class JsMagmaScriptEvaluatorTest {

    @Test
    fun `test$`() {
        val person = DynamicEntity(personWeightEntityType)
        person.set("weight", 82)

        val weight = jsMagmaScriptEvaluator!!.eval("$('weight').value()", person)
        assertEquals(weight, 82)
    }

    @Test
    fun testUnitConversion() {
        val person = DynamicEntity(personWeightEntityType)
        person.set("weight", 82)

        val weight = jsMagmaScriptEvaluator!!.eval("$('weight').unit('kg').toUnit('poundmass').value()", person)
        assertEquals(weight, 180.7790549915996)
    }

    @Test
    fun mapSimple() {
        val gender = DynamicEntity(genderEntityType)
        gender.set("id", "B")

        val person = DynamicEntity(personGenderEntityType)
        person.set("gender", gender)

        val result = jsMagmaScriptEvaluator!!.eval("$('gender').map({'20':'2','B':'B2'}).value()", person)
        assertEquals(result!!.toString(), "B2")
    }

    @Test
    fun mapDefault() {
        val gender = DynamicEntity(genderEntityType)
        gender.set("id", "B")

        val person = DynamicEntity(personGenderEntityType)
        person.set("gender", gender)

        val result = jsMagmaScriptEvaluator!!.eval("$('gender').map({'20':'2'}, 'B2').value()", person)
        assertEquals(result!!.toString(), "B2")
    }

    @Test
    fun mapNull() {
        val result = jsMagmaScriptEvaluator!!.eval("$('gender').map({'20':'2'}, 'B2', 'B3').value()",
                DynamicEntity(personGenderEntityType))
        assertEquals(result!!.toString(), "B3")
    }

    @Test
    fun testAverageValueOfMultipleNumericAttributes() {
        val sbp1Attr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("SBP_1").getMock<Attribute>()
        `when`<AttributeType>(sbp1Attr.dataType).thenReturn(DECIMAL)
        val sbp2Attr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("SBP_2").getMock<Attribute>()
        `when`<AttributeType>(sbp2Attr.dataType).thenReturn(DECIMAL)
        val sbpPersonEntityType = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("person").getMock<EntityType>()
        `when`(sbpPersonEntityType.getAttribute("SBP_1")).thenReturn(sbp1Attr)
        `when`(sbpPersonEntityType.getAttribute("SBP_2")).thenReturn(sbp2Attr)
        `when`(sbpPersonEntityType.atomicAttributes).thenReturn(asList(sbp1Attr, sbp2Attr))

        val entity0 = DynamicEntity(sbpPersonEntityType)
        entity0.set("SBP_1", 120.0)
        entity0.set("SBP_2", 124.0)

        val entity1 = DynamicEntity(sbpPersonEntityType)
        entity1.set("SBP_1", 120.0)

        val entity2 = DynamicEntity(sbpPersonEntityType)

        val script = "var counter = 0;\nvar SUM=newValue(0);\nif(!$('SBP_1').isNull().value()){\n\tSUM.plus($('SBP_1').value());\n\tcounter++;\n}\nif(!$('SBP_2').isNull().value()){\n\tSUM.plus($('SBP_2').value());\n\tcounter++;\n}\nif(counter !== 0){\n\tSUM.div(counter);\nSUM.value();\n}\nelse{\n\tnull;\n}"
        val result1 = jsMagmaScriptEvaluator!!.eval(script, entity0)
        assertEquals(result1!!.toString(), "122.0")

        val result2 = jsMagmaScriptEvaluator!!.eval(script, entity1)
        assertEquals(result2!!.toString(), "120.0")

        val result3 = jsMagmaScriptEvaluator!!.eval(script, entity2)
        assertEquals(result3, null)
    }

    @Test
    fun testGroup() {
        val entity1 = DynamicEntity(personAgeEntityType)
        entity1.set("age", 29)

        val result1 = jsMagmaScriptEvaluator!!.eval("$('age').group([18, 35, 56]).value();", entity1)
        assertEquals(result1!!.toString(), "18-35")

        val entity2 = DynamicEntity(personAgeEntityType)
        entity2.set("age", 999)

        val result2 = jsMagmaScriptEvaluator!!.eval("$('age').group([18, 35, 56], [888, 999]).value();", entity2)
        assertEquals(result2!!.toString(), "999")

        val entity3 = DynamicEntity(personAgeEntityType)
        entity3.set("age", 47)

        val result3 = jsMagmaScriptEvaluator!!.eval("$('age').group([18, 35, 56]).value();", entity3)
        assertEquals(result3!!.toString(), "35-56")
    }

    @Test
    fun testGroupNull() {
        val entity4 = DynamicEntity(personAgeEntityType)
        entity4.set("age", 47)

        val result4 = jsMagmaScriptEvaluator!!.eval("$('age').group().value();", entity4)
        assertEquals(result4, null)

        val result5 = jsMagmaScriptEvaluator!!.eval("$('age').group([56, 18, 35]).value();", entity4)
        assertEquals(result5, null)

        val result6 = jsMagmaScriptEvaluator!!.eval("$('age').group([56, 18, 35], null,'123456').value();", entity4)
        assertEquals(result6!!.toString(), "123456")
    }

    @Test
    fun testGroupConstantValue() {
        val entity4 = DynamicEntity(personAgeEntityType)
        entity4.set("age", 47)

        val result4 = jsMagmaScriptEvaluator!!.eval(
                "var age_variable=new newValue(45);age_variable.group([18, 35, 56]).value();", entity4)
        assertEquals(result4!!.toString(), "35-56")
    }

    @Test
    fun combineGroupMapFunctions() {
        val entity1 = DynamicEntity(personAgeEntityType)
        entity1.set("age", 29)

        val result1 = jsMagmaScriptEvaluator!!.eval(
                "$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();", entity1)
        assertEquals(result1!!.toString(), "1")

        val entity2 = DynamicEntity(personAgeEntityType)
        entity2.set("age", 17)

        val result2 = jsMagmaScriptEvaluator!!.eval(
                "$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();", entity2)
        assertEquals(result2!!.toString(), "0")

        val entity3 = DynamicEntity(personAgeEntityType)
        entity3.set("age", 40)

        val result3 = jsMagmaScriptEvaluator!!.eval(
                "$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();", entity3)
        assertEquals(result3!!.toString(), "2")

        val entity4 = DynamicEntity(personAgeEntityType)
        entity4.set("age", 70)

        val result4 = jsMagmaScriptEvaluator!!.eval(
                "$('age').group([18, 35, 56]).map({'-18':'0','18-35':'1','35-56':'2','56+':'3'}).value();", entity4)
        assertEquals(result4!!.toString(), "3")

        val entity5 = DynamicEntity(personAgeEntityType)
        entity5.set("age", 999)

        val result5 = jsMagmaScriptEvaluator!!.eval(
                "$('age').group([18, 35, 56], [999]).map({'-18':0,'18-35':1,'35-56':2,'56+':3,'999':'9'}).value();",
                entity5)
        assertEquals(result5!!.toString(), "9")
    }

    @Test
    fun combinePlusGroupMapFunctions() {
        val food59Attr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("FOOD59A1").getMock<Attribute>()
        `when`<AttributeType>(food59Attr.dataType).thenReturn(INT)
        val food60Attr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("FOOD60A1").getMock<Attribute>()
        `when`<AttributeType>(food60Attr.dataType).thenReturn(INT)
        val foodPersonEntityType = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("person").getMock<EntityType>()
        `when`(foodPersonEntityType.getAttribute("FOOD59A1")).thenReturn(food59Attr)
        `when`(foodPersonEntityType.getAttribute("FOOD60A1")).thenReturn(food60Attr)
        `when`(foodPersonEntityType.atomicAttributes).thenReturn(asList(food59Attr, food60Attr))

        val entity0 = DynamicEntity(foodPersonEntityType)
        entity0.set("FOOD59A1", 7)
        entity0.set("FOOD60A1", 6)

        val result1 = jsMagmaScriptEvaluator!!.eval(
                "var SUM_WEIGHT = new newValue(0);SUM_WEIGHT.plus($('FOOD59A1').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());SUM_WEIGHT.plus($('FOOD60A1').map({\"1\":0,\"2\":0.2,\"3\":0.6,\"4\":1,\"5\":2.5,\"6\":4.5,\"7\":6.5}, null, null).value());SUM_WEIGHT.group([0,1,2,6,7]).map({\"0-1\":\"4\",\"1-2\":\"3\",\"2-6\":\"2\",\"6-7\":\"1\", \"7+\" : \"1\"},null,null).value();",
                entity0)

        assertEquals(result1!!.toString(), "1")
    }

    @Test
    fun testPlusValue() {
        val entity0 = DynamicEntity(personHeightEntityType)
        entity0.set("height", 180)
        val result = jsMagmaScriptEvaluator!!.eval("$('height').plus(100).value()", entity0)
        assertEquals(result, 280.toDouble())
    }

    @Test
    fun testPlusObject() {
        val entity0 = DynamicEntity(personHeightEntityType)
        entity0.set("height", 180)
        val result1 = jsMagmaScriptEvaluator!!.eval("$('height').plus(new newValue(100)).value()", entity0)
        assertEquals(result1, 280.toDouble())
    }

    @Test
    fun testPlusNullValue() {
        val entity0 = DynamicEntity(personHeightEntityType)
        entity0.set("height", 180)
        val result1 = jsMagmaScriptEvaluator!!.eval("$('height').plus(null).value()", entity0)
        assertEquals(result1, 180)
    }

    @Test
    fun testTimes() {
        val entity0 = DynamicEntity(personHeightEntityType)
        entity0.set("height", 2)
        val result = jsMagmaScriptEvaluator!!.eval("$('height').times(100).value()", entity0)
        assertEquals(result, 200.toDouble())
    }

    @Test
    fun div() {
        val entity0 = DynamicEntity(personHeightEntityType)
        entity0.set("height", 200)
        val result = jsMagmaScriptEvaluator!!.eval("$('height').div(100).value()", entity0)
        assertEquals(result, 2.0)
    }

    @Test
    fun pow() {
        val entity0 = DynamicEntity(personHeightEntityType)
        entity0.set("height", 20)
        val result = jsMagmaScriptEvaluator!!.eval("$('height').pow(2).value()", entity0)
        assertEquals(result, 400.0)
    }

    @Test
    fun testBmi() {
        val person = DynamicEntity(personWeightAndHeightEntityType)
        person.set("weight", 82)
        person.set("height", 189)

        val bmi = jsMagmaScriptEvaluator!!.eval("$('weight').div($('height').div(100).pow(2)).value()", person)
        val df = DecimalFormat("#.####", DecimalFormatSymbols(Locale.ENGLISH))
        assertEquals(df.format(bmi), df.format(82.0 / (1.89 * 1.89)))
    }

    @Test
    fun testGlucose() {
        val gluc1Attr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("GLUC_1").getMock<Attribute>()
        `when`<AttributeType>(gluc1Attr.dataType).thenReturn(DECIMAL)
        val personGlucoseMeta = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("glucose").getMock<EntityType>()
        `when`(personGlucoseMeta.getAttribute("GLUC_1")).thenReturn(gluc1Attr)
        `when`(personGlucoseMeta.atomicAttributes).thenReturn(listOf<Attribute>(gluc1Attr))

        val glucose = DynamicEntity(personGlucoseMeta)
        glucose.set("GLUC_1", 4.1)

        val bmi = jsMagmaScriptEvaluator!!.eval("$('GLUC_1').div(100).value()", glucose)
        val df = DecimalFormat("#.####", DecimalFormatSymbols(Locale.ENGLISH))
        assertEquals(df.format(bmi), df.format(4.1 / 100))
    }

    @Test
    fun age() {
        val person = DynamicEntity(personBirthDateMeta)
        person.set("birthdate", now().atOffset(UTC).toLocalDate())

        val result = jsMagmaScriptEvaluator!!.eval("$('birthdate').age().value()", person)
        assertEquals(result, 0.0)
    }

    @Test
    fun testNull() {
        val person0 = DynamicEntity(personBirthDateMeta)
        person0.set("birthdate", LocalDate.now())

        val script = "$('birthdate').age().value() < 18  || $('birthdate').value() != null"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, true)

        val person1 = DynamicEntity(personBirthDateMeta)
        person1.set("birthdate", null)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, false)
    }

    @Test
    fun testEq() {
        val person0 = DynamicEntity(personWeightEntityType)
        person0.set("weight", 100)
        val script = "$('weight').eq(100).value()"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, true)

        val person1 = DynamicEntity(personWeightEntityType)
        person1.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, false)
    }

    @Test
    fun testIsNull() {
        val person0 = DynamicEntity(personWeightEntityType)
        person0.set("weight", null)
        val script = "$('weight').isNull().value()"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, true)

        val person1 = DynamicEntity(personWeightEntityType)
        person1.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, false)
    }

    @Test
    fun testNot() {
        val person0 = DynamicEntity(personWeightEntityType)
        person0.set("weight", null)
        val script = "$('weight').isNull().not().value()"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, false)

        val person1 = DynamicEntity(personWeightEntityType)
        person1.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, true)
    }

    @Test
    fun testOr() {
        val person0 = DynamicEntity(personWeightEntityType)
        person0.set("weight", null)
        val script = "$('weight').eq(99).or($('weight').eq(100)).value()"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, false)

        val person1 = DynamicEntity(personWeightEntityType)
        person1.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, true)

        val person2 = DynamicEntity(personWeightEntityType)
        person2.set("weight", 100)
        result = jsMagmaScriptEvaluator!!.eval(script, person2)
        assertEquals(result, true)

        val person3 = DynamicEntity(personWeightEntityType)
        person3.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person3)
        assertEquals(result, true)
    }

    @Test
    fun testGt() {
        val person0 = DynamicEntity(personWeightEntityType)
        person0.set("weight", null)
        val script = "$('weight').gt(100).value()"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, false)

        val person1 = DynamicEntity(personWeightEntityType)
        person1.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, false)

        val person2 = DynamicEntity(personWeightEntityType)
        person2.set("weight", 100)
        result = jsMagmaScriptEvaluator!!.eval(script, person2)
        assertEquals(result, false)

        val person3 = DynamicEntity(personWeightEntityType)
        person3.set("weight", 101)
        result = jsMagmaScriptEvaluator!!.eval(script, person3)
        assertEquals(result, true)
    }

    @Test
    fun testLt() {
        val person0 = DynamicEntity(personWeightEntityType)
        person0.set("weight", null)
        val script = "$('weight').lt(100).value()"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, false)

        val person1 = DynamicEntity(personWeightEntityType)
        person1.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, true)

        val person2 = DynamicEntity(personWeightEntityType)
        person2.set("weight", 100)
        result = jsMagmaScriptEvaluator!!.eval(script, person2)
        assertEquals(result, false)

        val person3 = DynamicEntity(personWeightEntityType)
        person3.set("weight", 101)
        result = jsMagmaScriptEvaluator!!.eval(script, person3)
        assertEquals(result, false)
    }

    @Test
    fun testGe() {
        val person0 = DynamicEntity(personWeightEntityType)
        person0.set("weight", null)
        val script = "$('weight').ge(100).value()"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, false)

        val person1 = DynamicEntity(personWeightEntityType)
        person1.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, false)

        val person2 = DynamicEntity(personWeightEntityType)
        person2.set("weight", 100)
        result = jsMagmaScriptEvaluator!!.eval(script, person2)
        assertEquals(result, true)

        val person3 = DynamicEntity(personWeightEntityType)
        person3.set("weight", 101)
        result = jsMagmaScriptEvaluator!!.eval(script, person3)
        assertEquals(result, true)
    }

    @Test
    fun testLe() {
        val person0 = DynamicEntity(personWeightEntityType)
        person0.set("weight", null)
        val script = "$('weight').le(100).value()"

        var result = jsMagmaScriptEvaluator!!.eval(script, person0)
        assertEquals(result, false)

        val person1 = DynamicEntity(personWeightEntityType)
        person1.set("weight", 99)
        result = jsMagmaScriptEvaluator!!.eval(script, person1)
        assertEquals(result, true)

        val person2 = DynamicEntity(personWeightEntityType)
        person2.set("weight", 100)
        result = jsMagmaScriptEvaluator!!.eval(script, person2)
        assertEquals(result, true)

        val person3 = DynamicEntity(personWeightEntityType)
        person3.set("weight", 101)
        result = jsMagmaScriptEvaluator!!.eval(script, person3)
        assertEquals(result, false)
    }

    companion object {
        private var personWeightEntityType: EntityType? = null
        private var personHeightEntityType: EntityType? = null
        private var personWeightAndHeightEntityType: EntityType? = null
        private var personBirthDateMeta: EntityType? = null
        private var personAgeEntityType: EntityType? = null
        private var genderEntityType: EntityType? = null
        private var personGenderEntityType: EntityType? = null

        private var jsMagmaScriptEvaluator: JsMagmaScriptEvaluator? = null

        @BeforeClass
        protected fun beforeClass() {
            val weightAttr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("weight").getMock<Attribute>()
            `when`<AttributeType>(weightAttr.dataType).thenReturn(INT)
            personWeightEntityType = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("person").getMock<EntityType>()
            `when`(personWeightEntityType!!.getAttribute("weight")).thenReturn(weightAttr)
            `when`(personWeightEntityType!!.atomicAttributes).thenReturn(listOf<Attribute>(weightAttr))

            val heightAttr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("height").getMock<Attribute>()
            `when`<AttributeType>(heightAttr.dataType).thenReturn(INT)
            personHeightEntityType = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("person").getMock<EntityType>()
            `when`(personHeightEntityType!!.getAttribute("height")).thenReturn(heightAttr)
            `when`(personHeightEntityType!!.atomicAttributes).thenReturn(listOf<Attribute>(heightAttr))

            personWeightAndHeightEntityType = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("person").getMock<EntityType>()
            `when`(personWeightAndHeightEntityType!!.getAttribute("weight")).thenReturn(weightAttr)
            `when`(personWeightAndHeightEntityType!!.getAttribute("height")).thenReturn(heightAttr)
            `when`(personWeightAndHeightEntityType!!.atomicAttributes).thenReturn(asList(weightAttr, heightAttr))

            val birthDateAttr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("birthdate").getMock<Attribute>()
            `when`<AttributeType>(birthDateAttr.dataType).thenReturn(DATE)
            personBirthDateMeta = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("person").getMock<EntityType>()
            `when`(personBirthDateMeta!!.getAttribute("birthdate")).thenReturn(birthDateAttr)
            `when`(personBirthDateMeta!!.atomicAttributes).thenReturn(listOf<Attribute>(birthDateAttr))

            val ageAttr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("age").getMock<Attribute>()
            `when`<AttributeType>(ageAttr.dataType).thenReturn(INT)
            personAgeEntityType = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("person").getMock<EntityType>()
            `when`(personAgeEntityType!!.getAttribute("age")).thenReturn(ageAttr)
            `when`(personAgeEntityType!!.atomicAttributes).thenReturn(listOf<Attribute>(ageAttr))

            val idAttr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("id").getMock<Attribute>()
            `when`<AttributeType>(idAttr.dataType).thenReturn(STRING)
            genderEntityType = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("gender").getMock<EntityType>()
            `when`(genderEntityType!!.idAttribute).thenReturn(idAttr)
            `when`(genderEntityType!!.getAttribute("id")).thenReturn(idAttr)
            `when`(genderEntityType!!.atomicAttributes).thenReturn(listOf<Attribute>(idAttr))

            val genderAttr = `when`(mock<Attribute>(Attribute::class.java).name).thenReturn("gender").getMock<Attribute>()
            `when`<AttributeType>(genderAttr.dataType).thenReturn(CATEGORICAL)
            personGenderEntityType = `when`(mock<EntityType>(EntityType::class.java).id).thenReturn("person").getMock<EntityType>()
            `when`(personGenderEntityType!!.getAttribute("gender")).thenReturn(genderAttr)
            `when`(personGenderEntityType!!.atomicAttributes).thenReturn(listOf<Attribute>(genderAttr))

            jsMagmaScriptEvaluator = JsMagmaScriptEvaluator(NashornScriptEngine())
        }
    }
}
