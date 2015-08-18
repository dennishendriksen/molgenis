package org.molgenis.app.controller;

import static org.molgenis.app.controller.HomeController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ui.controller.AbstractStaticContentController;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends AbstractStaticContentController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public HomeController()
	{
		super(ID, URI);
	}

	@RequestMapping("/test")
	@ResponseStatus(HttpStatus.OK)
	public void test()
	{
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextProvider
				.getApplicationContext();
		ctx.refresh();
	}
}
