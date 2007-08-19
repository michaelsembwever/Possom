/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.result;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>Immutable</b>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class BasicSuggestion implements Suggestion{

    private static final Map<Integer,WeakReference<BasicSuggestion>> WEAK_CACHE
            = new ConcurrentHashMap<Integer,WeakReference<BasicSuggestion>>();

    private final String original;
    private final String suggestion;
    private final String htmlSuggestion;

    /**
     *
     * @param original
     * @param suggestion
     * @param htmlSuggestion
     * @return
     */
    public static final BasicSuggestion instanceOf(
            final String original,
            final String suggestion,
            final String htmlSuggestion){

        final int hashCode = hashCode(original, suggestion, htmlSuggestion);

        BasicSuggestion bs = null;

        if(WEAK_CACHE.containsKey(hashCode)){
            final WeakReference<BasicSuggestion> wk = WEAK_CACHE.get(hashCode);
            bs = wk.get();
        }

        if(null == bs){
            bs = new BasicSuggestion(original, suggestion, htmlSuggestion);
            WEAK_CACHE.put(hashCode, new WeakReference<BasicSuggestion>(bs));
        }

        return bs;
    }

    /**
     *
     * @param original
     * @param suggestion
     * @param htmlSuggestion
     */
    protected BasicSuggestion(final String original, final String suggestion, final String htmlSuggestion) {

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

    @Override
    public boolean equals(Object obj) {

        if( obj instanceof BasicSuggestion){

            final BasicSuggestion bs = (BasicSuggestion)obj;
            return original.equals(bs.original)
                    && suggestion.equals(bs.suggestion)
                    && htmlSuggestion.equals(bs.htmlSuggestion);

        }else{
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {

        return hashCode(original,suggestion, htmlSuggestion);
    }

    protected static final int hashCode(
            final String original,
            final String suggestion,
            final String htmlSuggestion){

        int result = 17;
        result = 37*result + original.hashCode();
        result = 37*result + suggestion.hashCode();
        result = 37*result + htmlSuggestion.hashCode();
        return result;
    }
}
