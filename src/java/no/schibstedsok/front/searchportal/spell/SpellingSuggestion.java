package no.schibstedsok.front.searchportal.spell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SpellingSuggestion implements Comparable {
    private static Log log = LogFactory.getLog(SpellingSuggestion.class);

    private String original;

    private String suggestion;
    private int score;

    public SpellingSuggestion(final String original, final String suggestion, final int score) {
        this.original = original;
        this.suggestion = suggestion;
        this.score = score;

        if (log.isDebugEnabled()) {
            log.debug("Creating spelling suggestion" + this);
        }
    }

    public String getOriginal() {
        return original;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public int getScore() {
        return score;
    }

    public String toString() {
        return getOriginal() + " " + getSuggestion() + "(" + getScore() + ")";
    }

    public int compareTo(Object o) {
        SpellingSuggestion suggestion = (SpellingSuggestion) o;
        return new Integer(suggestion.getScore()).compareTo(new Integer(this.score));
    }

}
