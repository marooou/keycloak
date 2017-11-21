package org.keycloak.storage.ldap.query;

import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.mappers.membership.group.GroupMapperConfig;
import org.keycloak.util.query.AttributeNotAllowedException;

class GroupLDAPMapping extends AbstractLDAPMapping {

    private static final String USER_MODEL_ATTRIBUTE = "groups";
    private final GroupMapperConfig config;

    GroupLDAPMapping(ComponentModel mapper) {
        super(mapper);
        this.config = new GroupMapperConfig(mapper);
    }

    @Override
    public String getUserModelAttribute() {
        return USER_MODEL_ATTRIBUTE;
    }

    @Override
    public String getLdapAttribute() {
        if (!config.getUserGroupsRetrieveStrategy().equals(GroupMapperConfig.GET_GROUPS_FROM_USER_MEMBEROF_ATTRIBUTE)) {
            throw new AttributeNotAllowedException(USER_MODEL_ATTRIBUTE,
                    "In order to filter by LDAP groups the user groups retrieve strategy must be set to: " +
                            GroupMapperConfig.GET_GROUPS_FROM_USER_MEMBEROF_ATTRIBUTE);
        }
        return config.getMemberOfLdapAttribute();
    }

    @Override
    public String getLdapValue(String value) {
        String groupNameLdapAttribute = config.getGroupNameLdapAttribute();
        String groupsDn = config.getGroupsDn();
        return String.format("%s=%s,%s", groupNameLdapAttribute, value, groupsDn);
    }
}
