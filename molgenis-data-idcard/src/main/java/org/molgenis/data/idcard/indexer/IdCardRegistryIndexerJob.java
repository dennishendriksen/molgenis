/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.indexer;

import org.molgenis.data.idcard.IdCardRegistryRepository;
import org.molgenis.data.idcard.model.IdCardRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public class IdCardRegistryIndexerJob extends IdCardIndexerJob<IdCardRegistry, IdCardRegistryRepository> {

    // Autowire by constructor not possible for Job classes
    @Autowired
    private IdCardRegistryRepository idCardBiobankRepository;

    @Override
    protected IdCardRegistryRepository getIdCardRepository() {
        return idCardBiobankRepository;
    }

}
