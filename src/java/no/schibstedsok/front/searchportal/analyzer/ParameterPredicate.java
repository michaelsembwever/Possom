package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.collections.Predicate;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class ParameterPredicate implements Predicate {
    private String paramName;
    private String paramValue;

    public ParameterPredicate(String paramName, String paramValue) {
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public boolean evaluate(Object object) {
        TokenEvaluatorFactory factory = (TokenEvaluatorFactory) object;
        return factory.getParameter(paramName).equals(paramValue);
    }
}
