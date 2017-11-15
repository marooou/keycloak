package org.keycloak.storage.ldap;


import org.junit.Before;
import org.junit.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.idm.query.EscapeStrategy;
import org.keycloak.util.query.FilterQuery;
import org.keycloak.util.query.FilterQueryVisitor;
import org.keycloak.util.query.AttributeNotAllowedException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LDAPFilterQueryVisitorTest {

    private LDAPFilterQueryVisitor visitor;

    @Before
    public void setUp() throws Exception {
        List<ComponentModel> mappers = new ArrayList<>();
        mappers.add(createFullNameMapper("cn"));
        mappers.add(createUserAttributeMapper("givenname", "firstName"));
        mappers.add(createUserAttributeMapper("sn", "lastName"));
        mappers.add(createUserAttributeMapper("uid", "username"));
        mappers.add(createUserAttributeMapper("mail", "email"));
        visitor = new LDAPFilterQueryVisitor(mappers, EscapeStrategy.DEFAULT);
    }

    @Test
    public void equalsComparison() {
        assertQuery("firstName equals \"John\"")
                .isTranslatedTo("(givenname=John)");
    }

    @Test
    public void lessThanComparison() {
        assertQuery("firstName lt \"John\"")
                .isTranslatedTo("(!(givenname>=John))");
    }

    @Test
    public void lessThanOrEqualsComparison() {
        assertQuery("firstName le \"John\"")
                .isTranslatedTo("(givenname<=John)");
    }

    @Test
    public void greaterThanComparison() {
        assertQuery("firstName gt \"John\"")
                .isTranslatedTo("(!(givenname<=John))");
    }

    @Test
    public void greaterThanOrEqualsComparison() {
        assertQuery("firstName ge \"John\"")
                .isTranslatedTo("(givenname>=John)");
    }

    @Test
    public void containsComparison() {
        assertQuery("firstName contains \"John\"")
                .isTranslatedTo("(givenname=*John*)");
    }

    @Test
    public void startsWithComparison() {
        assertQuery("firstName startswith \"John\"")
                .isTranslatedTo("(givenname=John*)");
    }

    @Test
    public void endsWithComparison() {
        assertQuery("firstName endswith \"John\"")
                .isTranslatedTo("(givenname=*John)");
    }

    @Test
    public void booleanComparison() {
        assertQuery("firstName equals true")
                .isTranslatedTo("(givenname=true)");
    }

    @Test
    public void notMappedAttributeUsed() {
        assertQuery("myCustomAttribute equals \"someValue\"")
                .hasNotAllowedAttribute("myCustomAttribute");
    }

    @Test
    public void notOperator() {
        assertQuery("not username equals \"jdoe\"")
                .isTranslatedTo("(!(uid=jdoe))");
    }

    @Test
    public void andOperator() {
        assertQuery("username equals \"jdoe\" and firstName equals \"John\"")
                .isTranslatedTo("(&(uid=jdoe)(givenname=John))");
    }

    @Test
    public void orOperator() {
        assertQuery("username equals \"jdoe\" or firstName equals \"John\"")
                .isTranslatedTo("(|(uid=jdoe)(givenname=John))");
    }

    @Test
    public void shouldEscapeAsterisk() {
        assertQuery("username equals \"*\"")
                .isTranslatedTo("(uid=\\2a)");
    }

    @Test
    public void inOperator() {
        assertQuery("firstName in (\"John\",\"Dom\")")
                .isTranslatedTo("(|(givenname=John)(givenname=Dom))");
    }

    @Test
    public void multipleOrs() {
        assertQuery("username equals \"a\" or username equals \"b\" or username equals \"c\"")
                .isTranslatedTo("(|(uid=a)(uid=b)(uid=c))");
    }

    @Test
    public void multipleAnds() {
        assertQuery("username equals \"a\" and username equals \"b\" and username equals \"c\"")
                .isTranslatedTo("(&(uid=a)(uid=b)(uid=c))");
    }

    @Test
    public void leftToRightComposition() {
        assertQuery("username equals \"a\" and username equals \"b\" or username equals \"c\"")
                .isTranslatedTo("(|(&(uid=a)(uid=b))(uid=c))");
    }

    @Test
    public void complexQuery() {
        assertQuery("username startswith \"j\" and (firstName in (\"John\", \"Janice\") or lastName equals \"Doe\")")
                .isTranslatedTo("(&(uid=j*)(|(|(givenname=John)(givenname=Janice))(sn=Doe)))");
    }

    /* Test utilities */

    private static ComponentModel createUserAttributeMapper(String ldapAttribute, String userModelAttribute) {
        ComponentModel componentModel = new ComponentModel();
        componentModel.setProviderId("user-attribute-ldap-mapper");
        componentModel.put("ldap.attribute", ldapAttribute);
        componentModel.put("user.model.attribute", userModelAttribute);
        return componentModel;
    }

    private static ComponentModel createFullNameMapper(String ldapAttribute) {
        ComponentModel componentModel = new ComponentModel();
        componentModel.setProviderId("full-name-ldap-mapper");
        componentModel.put("ldap.full.name.attribute", ldapAttribute);
        return componentModel;
    }

    private static ComponentModel createGroupMapper() {
        ComponentModel componentModel = new ComponentModel();
        componentModel.setProviderId("group-ldap-mapper");
        componentModel.put("membership.attribute.type", "DN");
        componentModel.put("user.roles.retrieve.strategy", "LOAD_GROUPS_BY_MEMBER_ATTRIBUTE");
        componentModel.put("group.name.ldap.attribute", "cn");
        componentModel.put("membership.ldap.attribute", "uniqueMember");
        componentModel.put("membership.user.ldap.attribute", "uid");
        componentModel.put("groups.dn", "ou=groups,dc=example,dc=com");
        componentModel.put("groups.dn", "ou=groups,dc=example,dc=com");
        componentModel.put("group.object.classes", "groupOfUniqueNames");
        return componentModel;
    }

    private QueryAssertion assertQuery(String query) {
        try {
            FilterQuery filterQuery = FilterQuery.parse(query);
            return new QueryAssertion(filterQuery, visitor);
        } catch (ParseException e) {
            throw new AssertionError("Unable to parse '" + query + '\'', e);
        }
    }

    private static class QueryAssertion {
        private final FilterQuery filterQuery;
        private final FilterQueryVisitor<String> visitor;

        QueryAssertion(FilterQuery filterQuery, FilterQueryVisitor<String> visitor) {
            this.filterQuery = filterQuery;
            this.visitor = visitor;
        }

        public void isTranslatedTo(String equivalent) {
            assertEquals(equivalent, filterQuery.accept(visitor));
        }

        public void hasNotAllowedAttribute(String attributeName) {
            final String errorMessage = "This query should report an invalid '" + attributeName + "' attribute usage";
            try {
                filterQuery.accept(visitor);
                fail(errorMessage);
            } catch (AttributeNotAllowedException exception) {
                if (!attributeName.equals(exception.getAttributeName())) {
                    fail(errorMessage);
                }
            }
        }
    }
}
