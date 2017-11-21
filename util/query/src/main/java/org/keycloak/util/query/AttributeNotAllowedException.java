package org.keycloak.util.query;

public class AttributeNotAllowedException extends IllegalArgumentException {

    private final String attributeName;

    public AttributeNotAllowedException(String attributeName) {
        super("Attribute " + attributeName + " is not allowed in filter query", null);
        this.attributeName = attributeName;
    }

    public AttributeNotAllowedException(String attributeName, String additionalMessage) {
        super("Attribute " + attributeName + " is not allowed in filter query. " + additionalMessage, null);
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
