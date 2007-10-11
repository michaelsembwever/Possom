/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.result;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b> Immutable </b>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class BasicWeightedSuggestion extends BasicSuggestion implements WeightedSuggestion {
    
    private static final Map<Integer,WeakReference<BasicWeightedSuggestion>> WEAK_CACHE
            = new ConcurrentHashMap<Integer,WeakReference<BasicWeightedSuggestion>>();

    private int weight;
    
    /**
     * 
     * @param original 
     * @param suggestion 
     * @param htmlSuggestion 
     * @param weight 
     * @return 
     */
    public static BasicWeightedSuggestion instanceOf(
            final String original, 
            final String suggestion, 
            final String htmlSuggestion, 
            final int weight){
        
        final int hashCode = hashCode(original, suggestion, htmlSuggestion, weight);
        
        BasicWeightedSuggestion bws = null;
        
        if(WEAK_CACHE.containsKey(hashCode)){
            final WeakReference<BasicWeightedSuggestion> wk = WEAK_CACHE.get(hashCode);
            bws = wk.get();
        }
        
        if(null == bws){
            bws = new BasicWeightedSuggestion(original, suggestion, htmlSuggestion, weight);
            WEAK_CACHE.put(hashCode, new WeakReference<BasicWeightedSuggestion>(bws));
        }
        
        return bws;
    }    

    /** TODO comment me. *
     * @param original 
     * @param suggestion 
     * @param htmlSuggestion 
     * @param weight 
     */
    protected BasicWeightedSuggestion(
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

    @Override
    public boolean equals(Object obj) {
        
        if( obj instanceof BasicWeightedSuggestion){
            
            final BasicWeightedSuggestion bws = (BasicWeightedSuggestion)obj;
            return super.equals(bws)
                    && weight == bws.weight;
            
        }else{
            return super.equals(obj);
        }
    }

    
    @Override
    public int hashCode() {
        
        return hashCode(getOriginal(), getSuggestion(), getHtmlSuggestion(), weight);
    }
    
    private static final int hashCode(
            final String original, 
            final String suggestion, 
            final String htmlSuggestion, 
            final int weight){
        
        int result = hashCode(original, suggestion, htmlSuggestion);
        result = 37*result + weight;
        return result;
    }
}
