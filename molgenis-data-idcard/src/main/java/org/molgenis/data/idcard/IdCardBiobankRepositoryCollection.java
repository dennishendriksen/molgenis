/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard;

import org.molgenis.data.DataService;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class IdCardBiobankRepositoryCollection extends IdCardRepositoryCollection<IdCardBiobank> {

    @Autowired
    public IdCardBiobankRepositoryCollection(DataService dataService, IdCardRepository<IdCardBiobank> idCardBiobankRepository) {
        super(dataService, idCardBiobankRepository);
    }

    @Override
    public String getName() {
        return BASE_NAME + IdCardBiobank.ENTITY_NAME;
    }

    @Override
    protected String getEntityName() {
        return IdCardBiobank.ENTITY_NAME;
    }

}
