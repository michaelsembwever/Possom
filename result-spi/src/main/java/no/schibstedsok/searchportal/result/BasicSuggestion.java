// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

/**
 * <b>Immutable</b>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class BasicSuggestion implements Suggestion{

    private final String original;
    private final String suggestion;
    private final String htmlSuggestion;

    /**
     * 
     * @param original 
     * @param suggestion 
     * @param htmlSuggestion 
     */
    public BasicSuggestion(final String original, final String suggestion, final String htmlSuggestion) {
        
        this.original = original;
        this.htmlSuggestion = htmlSuggestion;
        this.suggestion = suggestion;
    }

    /**
     * 
     * @return 
     */
    public String getOriginal() {
        return original;
    }
    
    /**
     * 
     * @return 
     */
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * 
     * @return 
     */
    public String getHtmlSuggestion() {
        return htmlSuggestion;
    }
}
