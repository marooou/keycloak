package org.keycloak.storage.ldap.query;

import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.mappers.UserAttributeLDAPStorageMapper;

class UserAttributeLDAPMapping extends AbstractLDAPMapping {

    UserAttributeLDAPMapping(ComponentModel mapper) {
        super(mapper);
    }

    @Override
    public String getUserModelAttribute() {
        return getMapper().get(UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE);
    }

    @Override
    public String getLdapAttribute() {
        return getMapper().get(UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE);
    }
}
