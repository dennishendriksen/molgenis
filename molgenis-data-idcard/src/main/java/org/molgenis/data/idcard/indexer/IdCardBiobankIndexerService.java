/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.indexer;

import org.molgenis.data.DataService;
import org.molgenis.data.idcard.IdCardBiobankRepository;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class IdCardBiobankIndexerService extends AbstractIdCardIndexerService<IdCardBiobank, IdCardBiobankRepository, IdCardBiobankIndexerJob> {

    @Autowired
    public IdCardBiobankIndexerService(DataService dataService, IdCardIndexerSettings idCardBiobankIndexerSettings, Scheduler scheduler) {
        super(dataService, idCardBiobankIndexerSettings, scheduler);
    }

    @Override
    protected Class<IdCardBiobankIndexerJob> getIdCardIndexerJob() {
        return IdCardBiobankIndexerJob.class;
    }

    @Override
    protected String getJobKey() {
        return "biobankIndexRebuild";
    }


    @Override
    protected String getJobName() {
        return "biobankJob";
    }

}
