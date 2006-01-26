package no.schibstedsok.front.searchportal.query;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class InfopageQueryTransformer extends AbstractQueryTransformer {
    public String getTransformedQuery(final Context cxt) {
        
        final String originalQuery = cxt.getQueryString();

        return "recordid:" + originalQuery;
    }
}
