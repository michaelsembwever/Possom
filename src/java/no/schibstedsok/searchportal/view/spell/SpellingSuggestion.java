// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.view.spell;

import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class SpellingSuggestion implements Comparable {
    private static final Logger LOG = Logger.getLogger(SpellingSuggestion.class);

    private String original;

    private String suggestion;
    private int score;

    /** TODO comment me. **/
    public SpellingSuggestion(final String original, final String suggestion, final int score) {
        this.original = original;
        this.suggestion = suggestion;
        this.score = score;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating spelling suggestion" + this);
        }
    }

    /** TODO comment me. **/
    public String getOriginal() {
        return original;
    }

    /** TODO comment me. **/
    public String getSuggestion() {
        return suggestion;
    }

    /** TODO comment me. **/
    public void setSuggestion(final String suggestion) {
        this.suggestion = suggestion;
    }

    /** TODO comment me. **/
    public int getScore() {
        return score;
    }

    /** TODO comment me. **/
    public String toString() {
        return getOriginal() + " " + getSuggestion() + "(" + getScore() + ")";
    }

    /** @inherit **/
    public int compareTo(final Object o) {
        final SpellingSuggestion suggestion = (SpellingSuggestion) o;
        return Integer.valueOf(suggestion.getScore()).compareTo(Integer.valueOf(this.score));
    }

}
