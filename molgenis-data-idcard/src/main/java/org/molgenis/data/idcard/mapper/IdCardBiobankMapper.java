/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.mapper;

import org.molgenis.data.DataService;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankMapper extends AbtractIdCardEntityMapper<IdCardBiobank> {

    @Autowired
    public IdCardBiobankMapper(DataService dataService) {
        super(dataService);
    }


    @Override
    protected IdCardBiobank getInstance() {
        return new IdCardBiobank(dataService);
    }

}
