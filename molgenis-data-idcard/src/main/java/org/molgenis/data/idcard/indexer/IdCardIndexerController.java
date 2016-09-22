package org.molgenis.data.idcard.indexer;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.idcard.indexer.IdCardIndexerController.URI;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.idcard.model.IdCardRegistry;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(IdCardIndexerController.URI)
public class IdCardIndexerController extends MolgenisPluginController {

    private static final Logger LOG = LoggerFactory.getLogger(IdCardIndexerController.class);

    public static final String ID = "idcardindexer";
    public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

    private final IdCardBiobankIndexerService biobankIndexerService;
    private final IdCardRegistryIndexerService registryIndexerService;

    @Autowired
    public IdCardIndexerController(IdCardBiobankIndexerService biobankIndexerService, IdCardRegistryIndexerService registryIndexerService) {
        super(URI);
        this.biobankIndexerService = requireNonNull(biobankIndexerService);
        this.registryIndexerService = requireNonNull(registryIndexerService);
    }

    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_SU, ROLE_ENTITY_READ_IDCARDINDEXER')")
    public String init(Model model) throws Exception {
        model.addAttribute("id_card_registry_entity_name", IdCardRegistry.ENTITY_NAME);
        model.addAttribute("id_card_biobank_entity_name", IdCardBiobank.ENTITY_NAME);
        return "view-idcardbiobankindexer";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reindex")
    @PreAuthorize("hasAnyRole('ROLE_SU, ROLE_ENTITY_WRITE_IDCARDINDEXER')")
    @ResponseBody
    public IndexRebuildStatus scheduleIndexRebuild(Model model) throws Exception {
        TriggerKey biobankKey = biobankIndexerService.scheduleIndexRebuild();
        TriggerState biobankStatus = biobankIndexerService.getIndexRebuildStatus(biobankKey);
        TriggerKey registryKey = registryIndexerService.scheduleIndexRebuild();
        TriggerState registryStatus = registryIndexerService.getIndexRebuildStatus(registryKey);
        return new IndexRebuildStatus(biobankKey, biobankStatus);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/status/{triggerGroup}/{triggerName}")
    @PreAuthorize("hasAnyRole('ROLE_SU, ROLE_ENTITY_READ_IDCARDINDEXER')")
    @ResponseBody
    public IndexRebuildStatus getIndexRebuildStatus(@PathVariable String triggerGroup, @PathVariable String triggerName)
            throws Exception {
        TriggerKey biobankKey = new TriggerKey(triggerName, triggerGroup);
        TriggerState biobankStatus = biobankIndexerService.getIndexRebuildStatus(biobankKey);
        TriggerKey registryKey = new TriggerKey(triggerName, triggerGroup);
        TriggerState registryStatus = registryIndexerService.getIndexRebuildStatus(registryKey);
        return new IndexRebuildStatus(biobankKey, biobankStatus);
    }

    @ExceptionHandler(value = Throwable.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessageResponse handleThrowable(Throwable t) {
        LOG.error("", t);
        return new ErrorMessageResponse(new ErrorMessage(t.getMessage()));
    }

    private static class IndexRebuildStatus {

        private final String triggerName;
        private final String triggerGroup;
        private final String triggerStatus;

        public IndexRebuildStatus(TriggerKey triggerKey, TriggerState triggerStatus) {
            this.triggerName = requireNonNull(triggerKey).getName();
            this.triggerGroup = requireNonNull(triggerKey).getGroup();
            this.triggerStatus = requireNonNull(triggerStatus).toString();
        }

        @SuppressWarnings("unused")
        public String getTriggerName() {
            return triggerName;
        }

        @SuppressWarnings("unused")
        public String getTriggerGroup() {
            return triggerGroup;
        }

        @SuppressWarnings("unused")
        public String getTriggerStatus() {
            return triggerStatus;
        }
    }
}
