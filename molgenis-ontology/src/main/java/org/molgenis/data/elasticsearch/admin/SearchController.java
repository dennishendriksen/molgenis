package org.molgenis.data.elasticsearch.admin;

import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.elasticsearch.admin.SearchController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class SearchController extends MolgenisPluginController
{
	public static final String ID = "search";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final SearchService searchService;

	@Autowired
	public SearchController(SearchService searchService)
	{
		super(URI);
		this.searchService = requireNonNull(searchService);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		return "view-search";
	}

	@RequestMapping(method = POST)
	@ResponseBody
	public SearchResponse search(@Valid @RequestBody SearchRequest searchRequest)
	{
		return searchService.search(searchRequest);
	}

	@RequestMapping(value = "/aggregate", method = POST)
	@ResponseBody
	public AggregationResponse aggregate(@Valid @RequestBody AggregateRequest aggregateRequest)
	{
		return searchService.aggregate(aggregateRequest);
	}
}
