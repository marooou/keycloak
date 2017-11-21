package org.keycloak.storage.ldap.query;

import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.mappers.FullNameLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.UserAttributeLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.membership.group.GroupLDAPStorageMapperFactory;

import java.util.List;
import java.util.stream.Collectors;

public class LDAPMappingFactory {
    public LDAPMapping createMapping(ComponentModel mapper) {
        switch (mapper.getProviderId()) {
            case UserAttributeLDAPStorageMapperFactory.PROVIDER_ID:
                return new UserAttributeLDAPMapping(mapper);
            case GroupLDAPStorageMapperFactory.PROVIDER_ID:
                return new GroupLDAPMapping(mapper);
            case FullNameLDAPStorageMapperFactory.PROVIDER_ID:
                return new FullNameLDAPMapping(mapper);
            default:
                return null;
        }
    }

    public List<LDAPMapping> createMappings(List<ComponentModel> mappers) {
        return mappers.stream().map(m -> createMapping(m))
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }
}
