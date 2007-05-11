// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;


/**
 * <b> Immutable </b>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class BasicWeightedSuggestion extends BasicSuggestion implements WeightedSuggestion {

    private int weight;

    /** TODO comment me. *
     * @param original 
     * @param suggestion 
     * @param weight 
     */
    public BasicWeightedSuggestion(
            final String original, 
            final String suggestion, 
            final String htmlSuggestion, 
            final int weight) {
        
        super(original, suggestion, htmlSuggestion);
        this.weight = weight;
    }
    
    /** TODO comment me. *
     * @return 
     */
    public int getWeight() {
        return weight;
    }

    /** TODO comment me. **/
    public String toString() {
        return getOriginal() + " " + getSuggestion() + "(" + getWeight() + ")";
    }


    public int compareTo(WeightedSuggestion o) {
        return o.getWeight() - getWeight();
    }

}
