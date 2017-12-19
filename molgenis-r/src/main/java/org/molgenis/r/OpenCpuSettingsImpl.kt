package org.molgenis.r

import org.molgenis.data.meta.AttributeType.INT
import org.molgenis.data.meta.AttributeType.STRING
import org.molgenis.data.settings.DefaultSettingsEntity
import org.molgenis.data.settings.DefaultSettingsEntityType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class OpenCpuSettingsImpl : DefaultSettingsEntity(ID), OpenCpuSettings {

    override val scheme: String
        get() = getString(SCHEME)

    override val host: String
        get() = getString(HOST)

    override val port: Int
        get() = getInt(PORT)!!

    override val rootPath: String
        get() = getString(ROOT_PATH)

    @Component
    class Meta : DefaultSettingsEntityType(ID) {
        @Value("\${opencpu.uri.scheme:http}")
        private val defaultScheme: String? = null
        @Value("\${opencpu.uri.host:localhost}")
        private val defaultHost: String? = null
        @Value("\${opencpu.uri.port:8004}")
        private val defaultPort: String? = null
        @Value("\${opencpu.uri.path:/ocpu/}")
        private val defaultRootPath: String? = null

        override fun init() {
            super.init()
            label = "OpenCPU settings"
            description = "OpenCPU, a framework for embedded scientific computing and reproducible research, settings."
            addAttribute(SCHEME).setDefaultValue(defaultScheme)
                    .setNillable(false)
                    .setLabel("URI scheme").description = "Open CPU URI scheme (e.g. http)."
            addAttribute(HOST).setDefaultValue(defaultHost)
                    .setNillable(false)
                    .setLabel("URI host").description = "Open CPU URI host (e.g. localhost)."
            addAttribute(PORT).setDataType(INT)
                    .setDefaultValue(defaultPort)
                    .setNillable(false)
                    .setLabel("URI port").description = "Open CPU URI port (e.g. 8004)."
            addAttribute(ROOT_PATH).setDataType(STRING)
                    .setDefaultValue(defaultRootPath)
                    .setNillable(false)
                    .setLabel("URI path").description = "Open CPU URI root path (e.g. /ocpu/)."
        }

        companion object {

            internal val SCHEME = "scheme"
            internal val HOST = "host"
            internal val PORT = "port"
            internal val ROOT_PATH = "rootPath"
        }
    }

    companion object {
        private val serialVersionUID = 1L

        private val ID = "OpenCpuSettings"
    }
}
