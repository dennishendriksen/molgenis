/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.idcard.client.IdCardRegistryClient;
import org.molgenis.data.idcard.model.IdCardEntityMetaData;
import org.molgenis.data.idcard.model.IdCardRegistry;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdCardRegistryRepository extends IdCardRepository<IdCardRegistry> {
    private static final Logger LOG = LoggerFactory.getLogger(IdCardRegistryRepository.class);

    @Autowired
    public IdCardRegistryRepository(IdCardEntityMetaData<IdCardRegistry> idCardRegistryMetaData, IdCardRegistryClient idCardClient, ElasticsearchService elasticsearchService, DataService dataService, IdCardIndexerSettings idCardIndexerSettings) {
        super(idCardRegistryMetaData, idCardClient, elasticsearchService, dataService, idCardIndexerSettings);
         LOG.debug("Autowired registry repository");
   }

    @Override
    protected IdCardRegistry getInstance() {
        return new IdCardRegistry(dataService);
    }
}
