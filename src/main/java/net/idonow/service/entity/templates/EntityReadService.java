package net.idonow.service.entity.templates;

import net.idonow.entity.templates.AbstractPersistentObject;

import java.util.List;

public interface EntityReadService<T extends AbstractPersistentObject> {

    List<T> getAllEntities();

    T getEntity(Long entityId);

}
