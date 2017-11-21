package org.keycloak.storage.ldap.query;

import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.mappers.FullNameLDAPStorageMapper;

class FullNameLDAPMapping extends AbstractLDAPMapping {

    private static final String USER_MODEL_ATTRIBUTE = "fullName";

    FullNameLDAPMapping(ComponentModel mapper) {
        super(mapper);
    }

    @Override
    public String getLdapAttribute() {
        return this.getMapper().get(FullNameLDAPStorageMapper.LDAP_FULL_NAME_ATTRIBUTE);
    }

    @Override
    public String getUserModelAttribute() {
        return USER_MODEL_ATTRIBUTE;
    }
}
