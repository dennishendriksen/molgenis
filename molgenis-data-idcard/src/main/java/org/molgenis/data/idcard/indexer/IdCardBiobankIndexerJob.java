/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.indexer;

import org.molgenis.data.idcard.IdCardBiobankRepository;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.springframework.beans.factory.annotation.Autowired;

public class IdCardBiobankIndexerJob extends IdCardIndexerJob<IdCardBiobank, IdCardBiobankRepository> {

    // Autowire by constructor not possible for Job classes
    @Autowired
    private IdCardBiobankRepository idCardBiobankRepository;

    @Override
    protected IdCardBiobankRepository getIdCardRepository() {
        return idCardBiobankRepository;
    }

}
