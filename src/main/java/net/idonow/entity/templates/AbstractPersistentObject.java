package net.idonow.entity.templates;

import java.io.Serializable;
import java.util.Objects;

public abstract class AbstractPersistentObject implements Identity, Serializable {

    public static final String PFX_UNIQUE = "uk_";
    public static final String PFX_FOREIGN = "fk_";
    public static final String PFX_CHECK = "ck_";
    public static final String SFX_PKEY = "_pkey"; // Database specific (postgres adds "_pkey" to primary key constraint name)
    public static final String SFX_ID = "_id";


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!this.getClass().isInstance(o)) {
            return false;
        }
        AbstractPersistentObject object = (AbstractPersistentObject) o;
        return getId() != null && object.getId() != null && getId().equals(object.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) {
            return super.hashCode();
        }
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ id=" + getId() + " }";
    }

}
