package org.molgenis.app.controller

import org.molgenis.ui.controller.AbstractStaticContentController
import org.molgenis.web.PluginController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Controller that handles contact page requests
 */
@Controller
@RequestMapping(URI)
class BackgroundController : AbstractStaticContentController(ID, URI) {
    companion object {
        val ID = "background"
        val URI = PluginController.PLUGIN_URI_PREFIX + ID
    }
}
