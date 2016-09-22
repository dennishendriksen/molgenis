/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard;

import org.molgenis.data.DataService;
import org.molgenis.data.idcard.model.IdCardRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardRegistryRepositoryCollection extends IdCardRepositoryCollection<IdCardRegistry> {

    @Autowired
    public IdCardRegistryRepositoryCollection(DataService dataService, IdCardRepository<IdCardRegistry> idCardRegistryRepository) {
        super(dataService, idCardRegistryRepository);
    }

    @Override
    protected String getEntityName() {
        return IdCardRegistry.ENTITY_NAME;
    }

    @Override
    public String getName() {
        return BASE_NAME + IdCardRegistry.ENTITY_NAME;
    }


}
