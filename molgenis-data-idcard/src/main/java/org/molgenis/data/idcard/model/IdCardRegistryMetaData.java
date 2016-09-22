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
public final class IdCardRegistryMetaData extends IdCardEntityMetaData<IdCardRegistry> {

    public IdCardRegistryMetaData() {
        super(IdCardRegistry.ENTITY_NAME, IdCardRegistry.class);
        setDescription("Registry data from ID-Card");
        setLabel("Registries");
    }

    @Override
    protected String getBackendName() {
        return IdCardBiobankRepositoryCollection.BASE_NAME + IdCardRegistry.ENTITY_NAME;
    }

}
