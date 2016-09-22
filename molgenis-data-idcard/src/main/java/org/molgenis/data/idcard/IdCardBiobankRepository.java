/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.idcard.client.IdCardBiobankClient;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.idcard.model.IdCardEntityMetaData;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdCardBiobankRepository extends IdCardRepository<IdCardBiobank> {
    private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankRepository.class);

    @Autowired
    public IdCardBiobankRepository(IdCardEntityMetaData<IdCardBiobank> idCardBiobankMetaData, IdCardBiobankClient idCardClient, ElasticsearchService elasticsearchService, DataService dataService, IdCardIndexerSettings idCardIndexerSettings) {
        super(idCardBiobankMetaData, idCardClient, elasticsearchService, dataService, idCardIndexerSettings);
        LOG.debug("Autowired biobank repository");
    }

    @Override
    protected IdCardBiobank getInstance() {
        return new IdCardBiobank(dataService);
    }
}
