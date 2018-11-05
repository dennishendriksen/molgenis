package org.molgenis.navigator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.navigator.NavigatorController.URI;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import javax.annotation.Nullable;
import javax.validation.Valid;
import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class NavigatorController extends VuePluginController {
  public static final String ID = "navigator";
  public static final String URI = PLUGIN_URI_PREFIX + ID;

  private final NavigatorService navigatorService;

  NavigatorController(
      MenuReaderService menuReaderService,
      AppSettings appSettings,
      UserAccountService userAccountService,
      NavigatorService navigatorService) {
    super(URI, menuReaderService, appSettings, userAccountService);
    this.navigatorService = requireNonNull(navigatorService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    super.init(model, ID);
    return "view-navigator";
  }

  @GetMapping("/get")
  @ResponseBody
  public GetResourcesResponse getResources(
      @RequestParam(value = "folderId", required = false) @Nullable String folderId) {
    Folder folder = navigatorService.getFolder(folderId);
    List<Resource> resources = navigatorService.getResources(folderId);
    return GetResourcesResponse.create(folder, resources);
  }

  @GetMapping("/search")
  @ResponseBody
  public SearchResourcesResponse searchResources(@RequestParam(value = "query") String query) {
    List<Resource> resources = navigatorService.findResources(query);
    return SearchResourcesResponse.create(resources);
  }

  @PutMapping("/update")
  @ResponseStatus(OK)
  public void updateResource(@RequestBody @Valid UpdateResourceRequest updateResourceRequest) {
    navigatorService.updateResource(updateResourceRequest.getResource());
  }

  @PostMapping("/copy")
  @ResponseBody
  public JobResponse copyResources(@RequestBody @Valid CopyResourcesRequest copyResourcesRequest) {
    JobExecution jobExecution =
        navigatorService.copyResources(
            copyResourcesRequest.getResources(), copyResourcesRequest.getTargetFolderId());
    return toJobResponse(jobExecution);
  }

  @PostMapping("/download")
  @ResponseBody
  public JobResponse downloadResources(
      @RequestBody @Valid DownloadResourcesRequest downloadResourcesRequest) {
    JobExecution jobExecution =
        navigatorService.downloadResources(downloadResourcesRequest.getResources());
    return toJobResponse(jobExecution);
  }

  @PostMapping("/move")
  @ResponseStatus(OK)
  public void moveResources(@RequestBody @Valid MoveResourcesRequest moveResourcesRequest) {
    navigatorService.moveResources(
        moveResourcesRequest.getResources(), moveResourcesRequest.getTargetFolderId());
  }

  @DeleteMapping("/delete")
  @ResponseStatus(OK)
  public void deleteResources(@RequestBody @Valid DeleteResourcesRequest deleteItemsRequest) {
    navigatorService.deleteResources(deleteItemsRequest.getResources());
  }

  private JobResponse toJobResponse(JobExecution jobExecution) {
    return JobResponse.builder()
        .setJobId(jobExecution.getIdentifier())
        .setJobStatus(jobExecution.getStatus().toString())
        .build();
  }
}
