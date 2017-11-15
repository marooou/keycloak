package org.keycloak.util.query;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

final class FilterQueryErrorListener implements ANTLRErrorListener {

    private final List<ParseException> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        errors.add(new ParseException("Syntax error detected (" + msg + ')', charPositionInLine));
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        errors.add(new ParseException("Ambiguity detected", startIndex));
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        errors.add(new ParseException("Attempting full context", startIndex));
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        errors.add(new ParseException("Context sensitivity detected", startIndex));
    }

    public void ensureSuccessfulParsing() throws ParseException {
        if (errors.size() > 0) {
            throw errors.get(0);
        }
    }
}
