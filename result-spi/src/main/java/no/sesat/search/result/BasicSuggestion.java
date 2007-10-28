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
import org.apache.log4j.Logger;

/**
 * <b>Immutable</b>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class BasicSuggestion implements Suggestion{

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;
    
    private static final Map<Integer,WeakReference<BasicSuggestion>> WEAK_CACHE
            = new ConcurrentHashMap<Integer,WeakReference<BasicSuggestion>>(
            WEAK_CACHE_INITIAL_CAPACITY, 
            WEAK_CACHE_LOAD_FACTOR, 
            WEAK_CACHE_CONCURRENCY_LEVEL);

    private static final Logger LOG = Logger.getLogger(BasicSuggestion.class);
    
    
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
            WEAK_CACHE.put(hashCode, new WeakSuggestionReference<BasicSuggestion>(hashCode, bs, WEAK_CACHE));
            // log WEAK_CACHE size every 100 increments
            if(WEAK_CACHE.size() % 100 == 0){
                LOG.info("WEAK_CACHE.size is "  + WEAK_CACHE.size());
            }
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
    

    // required to keep size of WEAK_CACHE down regardless of null entries
    //  TODO make commons class (similar copies of this exist around)
    protected static final class WeakSuggestionReference<T> extends WeakReference<T>{

        private Map<Integer,WeakReference<T>> weakCache;
        private int key;

        WeakSuggestionReference(
                final int key,
                final T suggestion,
                final Map<Integer,WeakReference<T>> weakCache){

            super(suggestion);
            this.key = key;
            this.weakCache = weakCache;
        }

        @Override
        public void clear() {
            // clear the hashmap entry too!
            weakCache.remove(key);
            weakCache = null;
            key = 0;
            // clear the referent
            super.clear();
        }
    }    
}
