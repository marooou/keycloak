package org.keycloak.storage.ldap;

import org.antlr.v4.runtime.ParserRuleContext;
import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.idm.query.EscapeStrategy;
import org.keycloak.storage.ldap.mappers.UserAttributeLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.UserAttributeLDAPStorageMapperFactory;
import org.keycloak.util.query.AttributeNotAllowedException;
import org.keycloak.util.query.FilterQueryBaseVisitor;
import org.keycloak.util.query.FilterQueryParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class LDAPFilterQueryVisitor extends FilterQueryBaseVisitor<String> {

    private final Map<String, ComponentModel> mappers;
    private final EscapeStrategy escapeStrategy;

    public LDAPFilterQueryVisitor(List<ComponentModel> mappers, EscapeStrategy escapeStrategy) {
        this.escapeStrategy = escapeStrategy;
        this.mappers = mappers.stream()
               .filter(m -> m.getProviderId().equals(UserAttributeLDAPStorageMapperFactory.PROVIDER_ID))
               .collect(Collectors.toMap(
                   m -> m.get(UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE),
                   m -> m)
               );
    }

    @Override
    public String visitOrExpression(FilterQueryParser.OrExpressionContext ctx) {
        return processLogicalOperator(ctx, "(|%s)");
    }

    @Override
    public String visitAndExpression(FilterQueryParser.AndExpressionContext ctx) {
        return processLogicalOperator(ctx, "(&%s)");
    }

    @Override
    public String visitNotExpression(FilterQueryParser.NotExpressionContext ctx) {
        return processLogicalOperator(ctx, "(!%s)");
    }

    @Override
    public String visitBaseExpression(FilterQueryParser.BaseExpressionContext ctx) {
        return super.visitBaseExpression(ctx);
    }

    @Override
    public String visitComparisonExpression(FilterQueryParser.ComparisonExpressionContext ctx) {
        String userModelAttributeName = ctx.getChild(0).getText();
        String operator = ctx.getChild(1).getText();
        String value = ctx.getChild(2).getText();
        String ldapAttributeName = getLdapAttributeName(userModelAttributeName);
        String ldapValue = getLdapValue(value);
        switch (operator) {
            case "equals":
                return String.format("(%s=%s)", ldapAttributeName, ldapValue);
            case "startswith":
                return String.format("(%s=%s*)", ldapAttributeName, ldapValue);
            case "endswith":
                return String.format("(%s=*%s)", ldapAttributeName, ldapValue);
            case "contains":
                return String.format("(%s=*%s*)", ldapAttributeName, ldapValue);
            case "lt":
                return String.format("(!(%s>=%s))", ldapAttributeName, ldapValue, ldapAttributeName, ldapValue);
            case "le":
                return String.format("(%s<=%s)", ldapAttributeName, ldapValue);
            case "gt":
                return String.format("(!(%s<=%s))", ldapAttributeName, ldapValue, ldapAttributeName, ldapValue);
            case "ge":
                return String.format("(%s>=%s)", ldapAttributeName, ldapValue);
            default:
                return null;
        }
    }

    @Override
    public String visitInExpression(FilterQueryParser.InExpressionContext ctx) {
        List<String> ldapValues = new ArrayList<>();
        String userModelAttributeName = ctx.getChild(0).getText();
        String ldapAttributeName = getLdapAttributeName(userModelAttributeName);
        for (int idx = 3; idx < ctx.children.size() - 1; idx += 2) {
            String value = ctx.children.get(idx).getText();
            ldapValues.add(getLdapValue(value));
        }
        String joinedValues = ldapValues.stream()
                .map(v -> String.format("(%s=%s)", ldapAttributeName, v))
                .collect(Collectors.joining());
        return ldapValues.size() > 1
                ? String.format("(|%s)", joinedValues)
                : joinedValues;
    }

    @Override
    protected String aggregateResult(String aggregate, String nextResult) {
        return String.format("%s%s", emptyIfNull(aggregate), emptyIfNull(nextResult));
    }

    private String getLdapAttributeName(String userModelAttributeName) {
        ComponentModel mapper = this.mappers.get(userModelAttributeName);
        if (mapper == null) {
            throw new AttributeNotAllowedException(userModelAttributeName);
        }
        return mapper.get(UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE);
    }

    private String processLogicalOperator(ParserRuleContext ctx, String logicalFormat) {
        String childrenFormat = visitChildren(ctx);
        return ctx.children.size() > 1
                ? String.format(logicalFormat, childrenFormat)
                : childrenFormat;
    }

    private String getLdapValue(String value) {
        String actualValue = value.startsWith("\"")
                ? value.substring(1, value.length() - 1)
                : value;
        return escapeStrategy.escape(actualValue);
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
