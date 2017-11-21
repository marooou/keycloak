package org.keycloak.storage.ldap.query;

import org.keycloak.component.ComponentModel;

abstract class AbstractLDAPMapping implements LDAPMapping {

    private final ComponentModel mapper;

    AbstractLDAPMapping(ComponentModel mapper) {
        this.mapper = mapper;
    }

    @Override
    public ComponentModel getMapper() {
        return this.mapper;
    }

    @Override
    public String getLdapValue(String value) {
        return value;
    }
}
