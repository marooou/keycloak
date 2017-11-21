package org.keycloak.storage.ldap.query;

import org.keycloak.component.ComponentModel;

public interface LDAPMapping {

    ComponentModel getMapper();

    String getUserModelAttribute();

    String getLdapAttribute();

    String getLdapValue(String value);
}
