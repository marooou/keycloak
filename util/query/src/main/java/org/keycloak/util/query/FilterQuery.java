package org.keycloak.util.query;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.text.ParseException;

public final class FilterQuery {

    private final String expression;
    private final FilterQueryParser.QueryContext queryContext;

    private FilterQuery(String expression, FilterQueryParser.QueryContext queryContext) {
        this.expression = expression;
        this.queryContext = queryContext;
    }

    public static FilterQuery parse(String expression) throws ParseException {
        if (expression == null) {
            throw new IllegalArgumentException("The expression cannot be null.");
        }
        ANTLRErrorStrategy errorStrategy = new BailErrorStrategy();
        FilterQueryLexer lexer = new FilterQueryLexer(new ANTLRInputStream(expression));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FilterQueryParser parser = new FilterQueryParser(tokens);
        final FilterQueryErrorListener listener = new FilterQueryErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);
        FilterQueryParser.QueryContext queryContext = parser.query();
        listener.ensureSuccessfulParsing();
        return new FilterQuery(expression, queryContext);
    }

    public <T> T accept(FilterQueryVisitor<T> visitor) {
        return this.queryContext.accept(visitor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterQuery that = (FilterQuery) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public String toString() {
        return this.expression;
    }
}
