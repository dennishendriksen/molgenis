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
import org.molgenis.data.idcard.mapper.IdCardRegistryMapper;
import org.molgenis.data.idcard.model.IdCardRegistry;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdCardRegistryClient extends AbstractIdCardClient<IdCardRegistry> {

    private static final Logger LOG = LoggerFactory.getLogger(IdCardRegistryClient.class);

    @Autowired
    public IdCardRegistryClient(HttpClient httpClient, IdCardIndexerSettings idCardIndexerSettings, IdCardRegistryMapper idCardBiobankMapper) {
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
        String url = idCardIndexerSettings.getApiBaseUri() + '/' + idCardIndexerSettings.getRegistryCollectionSelectionResource() + '/' + value;
        LOG.debug("Registry url: {}", url);
        return url;
    }

    @Override
    protected String getCollectionURI() {
        String url = idCardIndexerSettings.getApiBaseUri() + '/' + idCardIndexerSettings.getRegistryCollectionResource();
        LOG.debug("Registry url: {}", url);
        return url;
    }

    @Override
    protected String getResourceURI(String id) {
        String url = idCardIndexerSettings.getApiBaseUri() + '/' + idCardIndexerSettings.getOrganisationResource() + '/' + id;
        LOG.debug("Registry url: {}", url);
        return url;
    }

}
