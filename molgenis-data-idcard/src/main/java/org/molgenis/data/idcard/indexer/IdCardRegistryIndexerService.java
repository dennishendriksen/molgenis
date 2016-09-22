/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.indexer;

import org.molgenis.data.DataService;
import org.molgenis.data.idcard.IdCardRegistryRepository;
import org.molgenis.data.idcard.model.IdCardRegistry;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdCardRegistryIndexerService extends AbstractIdCardIndexerService<IdCardRegistry, IdCardRegistryRepository, IdCardRegistryIndexerJob> {

    @Autowired
    public IdCardRegistryIndexerService(DataService dataService, IdCardIndexerSettings idCardBiobankIndexerSettings, Scheduler scheduler) {
        super(dataService, idCardBiobankIndexerSettings, scheduler);
    }

    @Override
    protected Class<IdCardRegistryIndexerJob> getIdCardIndexerJob() {
        return IdCardRegistryIndexerJob.class;
    }

    @Override
    protected String getJobKey() {
        return "rebuildRegistryIndex";
    }

    @Override
    protected String getJobName() {
        return "registryJob";
    }

}
