package org.molgenis.settings.controller;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.settings.SettingsPackage;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.google.common.collect.Streams.stream;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.EntityTypePermission.UPDATE_DATA;
import static org.molgenis.settings.controller.SettingsController.URI;

@Controller
@RequestMapping(URI)
public class SettingsController extends VuePluginController

{
	public static final String ID = "settings";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public static final String VIEW_TEMPLATE = "view-settings";
	private final MetaDataService metaDataService;
	private final UserPermissionEvaluator userPermissionEvaluator;

	SettingsController(MenuReaderService menuReaderService, AppSettings appSettings,
			UserAccountService userAccountService, MetaDataService metaDataService,
			UserPermissionEvaluator userPermissionEvaluator)
	{
		super(URI, menuReaderService, appSettings, userAccountService);
		this.metaDataService = requireNonNull(metaDataService);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
	}

	@GetMapping("/**")
	public String init(Model model)
	{
		super.init(model, ID);
		return VIEW_TEMPLATE;
	}

	@GetMapping("/types")
	@ResponseBody
	public SettingsTypesResponse getSettingsTypes()
	{
		Package settingsPackage = metaDataService.getPackage(SettingsPackage.PACKAGE_SETTINGS);
		List<String> entityTypeIds = stream(settingsPackage.getEntityTypes()).filter(this::canUpdateSettings)
																			 .sorted(comparing(EntityType::getLabel))
																			 .map(EntityType::getId)
																			 .collect(toList());
		return SettingsTypesResponse.create(entityTypeIds);
	}

	private boolean canUpdateSettings(EntityType entityType)
	{
		return !entityType.isAbstract() && userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType),
				UPDATE_DATA);
	}
}
