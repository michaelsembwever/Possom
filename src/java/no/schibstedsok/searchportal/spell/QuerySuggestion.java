package no.schibstedsok.searchportal.spell;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class QuerySuggestion {

    private String query;
    private String displayQuery;

    public QuerySuggestion(String querySuggestion, String displayQuerySuggestion) {
        this.displayQuery = displayQuerySuggestion;
        this.query = querySuggestion;
    }

    public String getQuery() {
        return query;
    }

    public String getDisplayQuery() {
        return displayQuery;
    }
}
