package bootstrap.molgenis;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebAppInitializationController
{
	@RequestMapping(method = GET)
	public String init()
	{
		return "view-bootstrap";
	}
}
