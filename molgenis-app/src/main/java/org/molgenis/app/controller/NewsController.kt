package org.molgenis.app.controller

import org.molgenis.ui.controller.AbstractStaticContentController
import org.molgenis.web.PluginController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Controller that handles news page requests
 */
@Controller
@RequestMapping(URI)
class NewsController : AbstractStaticContentController(ID, URI) {
    companion object {
        val ID = "news"
        val URI = PluginController.PLUGIN_URI_PREFIX + ID
    }
}
