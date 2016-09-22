/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.model;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;

/**
 *
 * @author <a href="mailto:david@allthingsdigital.nl">David van Enckevort</a>
 */
public class IdCardRegistry extends IdCardEntity {

    private static final long serialVersionUID = 1L;
    public static final String ENTITY_NAME = "rdconnect_reg";
    public static final EntityMetaData META_DATA = new IdCardBiobankMetaData();

    public IdCardRegistry(final DataService dataService) {
        super(META_DATA, dataService);
    }

}
