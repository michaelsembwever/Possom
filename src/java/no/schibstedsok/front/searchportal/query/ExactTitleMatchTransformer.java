package no.schibstedsok.front.searchportal.query;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class ExactTitleMatchTransformer extends AbstractQueryTransformer {
    public String getTransformedQuery(String originalQuery) {
        return originalQuery;
    }
}
