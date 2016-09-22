package org.molgenis.data.idcard.mapper;

import static org.molgenis.data.idcard.model.IdCardEntity.CITY;
import static org.molgenis.data.idcard.model.IdCardEntity.COUNTRY;
import static org.molgenis.data.idcard.model.IdCardEntity.NAME;
import static org.molgenis.data.idcard.model.IdCardEntity.NAME_OF_HOST_INSTITUTION;
import static org.molgenis.data.idcard.model.IdCardEntity.ORGANIZATION_ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.idcard.model.IdCardEntity;
import org.molgenis.data.idcard.model.IdCardOrganization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import org.molgenis.data.DataService;
import static org.molgenis.data.idcard.model.IdCardEntity.IDCARD_URL;

public abstract class AbtractIdCardEntityMapper<E extends IdCardEntity> implements IdCardEntityMapper<E> {

    private static final Logger LOG = LoggerFactory.getLogger(AbtractIdCardEntityMapper.class);

    protected final DataService dataService;

    protected AbtractIdCardEntityMapper(final DataService dataService) {
        this.dataService = dataService;
    }

    protected abstract E getInstance();

    @Override
    public E toIdCardBiobank(JsonReader jsonReader) throws IOException {
        E idCardBiobank = getInstance();

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "Collections":
                    jsonReader.skipValue(); // not used at the moment
                    break;
                case "OrganizationID":
                    idCardBiobank.set(ORGANIZATION_ID, jsonReader.nextString());
                    break;
                case "idcardurl":
                    idCardBiobank.set(IDCARD_URL, jsonReader.nextString());
                    break;
                case "address":
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        switch (jsonReader.nextName()) {
                            case "name of host institution":
                                idCardBiobank.set(NAME_OF_HOST_INSTITUTION, jsonReader.nextString());
                                break;
                            case "country":
                                idCardBiobank.set(COUNTRY, jsonReader.nextString());
                                break;
                            case "city":
                                idCardBiobank.set(CITY, jsonReader.nextString());
                                break;
                            default:
                                jsonReader.skipValue();
                                break;
                        }
                    }
                    jsonReader.endObject();
                    break;
                case "name":
                    idCardBiobank.set(NAME, jsonReader.nextString());
                    break;
                default:
                    LOG.debug("Ignored property [{}] in root object", name);
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();
        return idCardBiobank;

    }

    @Override
    public Iterable<Entity> toIdCardBiobanks(JsonReader jsonReader) throws IOException {
        List<Entity> idCardBiobanks = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            idCardBiobanks.add(toIdCardBiobank(jsonReader));
        }
        jsonReader.endArray();

        return idCardBiobanks;
    }

    @Override
    public IdCardOrganization toIdCardOrganization(JsonReader jsonReader) throws IOException {
        IdCardOrganization idCardOrganization = new IdCardOrganization();

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "Collections":
                    jsonReader.skipValue(); // not used at the moment
                    break;
                case "name":
                    idCardOrganization.setName(jsonReader.nextString());
                    break;
                case "ID":
                    idCardOrganization.setId(jsonReader.nextString());
                    break;
                case "OrganizationID":
                    idCardOrganization.setOrganizationId(jsonReader.nextString());
                    break;
                case "type":
                    idCardOrganization.setType(jsonReader.nextString());
                    break;
                default:
                    LOG.warn("unknown property [{}] in root object", name);
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();

        return idCardOrganization;
    }

    @Override
    public Iterable<IdCardOrganization> toIdCardOrganizations(JsonReader jsonReader) throws IOException {
        List<IdCardOrganization> idCardOrganizations = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            idCardOrganizations.add(toIdCardOrganization(jsonReader));
        }
        jsonReader.endArray();

        return idCardOrganizations;
    }
}
