/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.model;

import org.molgenis.data.idcard.IdCardBiobankRepositoryCollection;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:david@allthingsdigital.nl">David van Enckevort</a>
 */
@Component
public final class IdCardBiobankMetaData extends IdCardEntityMetaData<IdCardBiobank> {

    public IdCardBiobankMetaData() {
        super(IdCardBiobank.ENTITY_NAME, IdCardBiobank.class);
        setDescription("Biobank data from ID-Card");
        setLabel("Biobanks");
    }

    @Override
    protected String getBackendName() {
        return IdCardBiobankRepositoryCollection.BASE_NAME + IdCardBiobank.ENTITY_NAME;
    }

}
