package org.molgenis.js.nashorn

import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Created by Dennis on 2/17/2017.
 */
class NashornScriptEngineTest {

    @Test
    @Throws(Exception::class)
    fun testInvokeFunction() {
        val epoch = 1487342481434L
        assertEquals(nashornScriptEngine!!.invokeFunction("evalScript", "new Date($epoch)"), epoch)

    }

    @Test
    @Throws(Exception::class)
    fun testInvokeDateDMY() {
        val localDate = LocalDate.now()
        val script = String.format("new Date(%d,%d,%d)", localDate.year, localDate.month.value - 1,
                localDate.dayOfMonth)
        val epochMilli = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(nashornScriptEngine!!.invokeFunction("evalScript", script), epochMilli)
    }

    companion object {
        private var nashornScriptEngine: NashornScriptEngine? = null

        @BeforeClass
        fun setUpBeforeClass() {
            nashornScriptEngine = NashornScriptEngine()
        }
    }

}