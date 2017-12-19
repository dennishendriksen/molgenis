package org.molgenis.r

import org.molgenis.security.token.TokenExtractor
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping

import javax.servlet.http.HttpServletRequest

/**
 * Returns the molgenis R api client script
 */
@Controller
class MolgenisRController {

    @GetMapping(URI)
    fun showMolgenisRApiClient(request: HttpServletRequest, model: Model): String {
        val apiUrl: String
        if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host"))) {
            apiUrl = request.scheme + "://" + request.serverName + ":" + request.localPort + API_URI
        } else {
            apiUrl = request.scheme + "://" + request.getHeader("X-Forwarded-Host") + API_URI
        }

        // If the request contains a molgenis security token, use it
        val token = TokenExtractor.getToken(request)
        if (token != null) {
            model.addAttribute("token", token)
        }

        model.addAttribute("api_url", apiUrl)

        return "molgenis.R"
    }

    companion object {
        private val URI = "/molgenis.R"
        private val API_URI = "/api/"
    }
}
