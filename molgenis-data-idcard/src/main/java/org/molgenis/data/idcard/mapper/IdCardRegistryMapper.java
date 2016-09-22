/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.mapper;

import org.molgenis.data.DataService;
import org.molgenis.data.idcard.model.IdCardRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardRegistryMapper extends AbtractIdCardEntityMapper<IdCardRegistry> {

    @Autowired
    public IdCardRegistryMapper(DataService dataService) {
        super(dataService);
    }

    @Override
    protected IdCardRegistry getInstance() {
        return new IdCardRegistry(dataService);
    }

}
