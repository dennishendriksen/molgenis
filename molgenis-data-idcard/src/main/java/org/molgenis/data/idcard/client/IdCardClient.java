package org.molgenis.data.idcard.client;

import org.molgenis.data.Entity;
import org.molgenis.data.idcard.model.IdCardEntity;

/**
 * ID-Cards:
 * http://rd-connect.eu/platform/biobanks/id-cards-linking-up-rare-disease-research-across-the-world/
 * @param <E>
 */
public interface IdCardClient<E extends IdCardEntity> {

    /**
     * Return all biobanks from ID-Cards as entities
     *
     * @return
     */
    Iterable<Entity> getIdCardEntities();

    /**
     * Return all biobanks from ID-Cards as entities, throws an exception if the
     * request took longer than the given timeout.
     *
     * @param timeout request timeout in ms
     * @return
     */
    public Iterable<Entity> getIdCardEntities(long timeout);

    /**
     * Return a biobank by id from ID-Cards as entity
     *
     * @param id
     * @return
     */
    Entity getIdCardEntity(String id);

    /**
     * Return a biobank by id from ID-Cards as entity, throws an exception if
     * the request took longer than the given timeout.
     *
     * @param id
     * @param timeout request timeout in ms
     * @return
     */
    public Entity getIdCardEntity(String id, long timeout);

    /**
     * Return biobanks by ids from ID-Cards as entities
     *
     * @param ids
     * @return
     */
    Iterable<Entity> getIdCardEntities(Iterable<String> ids);

    /**
     * Return biobanks by ids from ID-Cards as entities, throws an exception if
     * the request took longer than the given timeout.
     *
     * @param ids
     * @param timeout the timeout value for the request
     * @return
     */
    public Iterable<Entity> getIdCardEntities(Iterable<String> ids, long timeout);
}
