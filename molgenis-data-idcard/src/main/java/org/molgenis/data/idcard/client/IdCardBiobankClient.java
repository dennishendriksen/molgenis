/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.molgenis.data.idcard.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.http.client.HttpClient;
import org.molgenis.data.idcard.mapper.IdCardBiobankMapper;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdCardBiobankClient extends AbstractIdCardClient<IdCardBiobank> {

    private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankClient.class);
    @Autowired
    public IdCardBiobankClient(final HttpClient httpClient, final IdCardIndexerSettings idCardIndexerSettings, final IdCardBiobankMapper idCardBiobankMapper) {
        super(httpClient, idCardIndexerSettings, idCardBiobankMapper);
        LOG.debug("Autowired {}", this.getClass().getName());
    }

    @Override
    protected String getCollectionSelectionURI(Iterable<String> ids) {
        String value = StreamSupport.stream(ids.spliterator(), false).collect(Collectors.joining(",", "[", "]"));
        try {
            value = URLEncoder.encode(value, UTF_8.name());
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
        return idCardIndexerSettings.getApiBaseUri() + '/' + idCardIndexerSettings.getBiobankCollectionSelectionResource() + '/' + value;
    }

    @Override
    protected String getCollectionURI() {
        return idCardIndexerSettings.getApiBaseUri() + '/' + idCardIndexerSettings.getBiobankCollectionResource();
    }

    @Override
    protected String getResourceURI(String id) {
        return idCardIndexerSettings.getApiBaseUri() + '/' + idCardIndexerSettings.getOrganisationResource() + '/' + id;
    }

}
