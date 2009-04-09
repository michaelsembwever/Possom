/* Copyright (2007) Schibsted ASA
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

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import no.sesat.commons.ref.ReferenceMap;
import org.apache.log4j.Logger;

/**
 * <b>Immutable</b>
 *
 * @version <tt>$Id$</tt>
 */
public class BasicSuggestion implements Suggestion{

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;

    private static final ReferenceMap<Integer,BasicSuggestion> WEAK_CACHE
            = new ReferenceMap<Integer,BasicSuggestion>(
                ReferenceMap.Type.WEAK,
                new ConcurrentHashMap<Integer,Reference<BasicSuggestion>>(
                    WEAK_CACHE_INITIAL_CAPACITY,
                    WEAK_CACHE_LOAD_FACTOR,
                    WEAK_CACHE_CONCURRENCY_LEVEL));

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

        BasicSuggestion bs = WEAK_CACHE.get(hashCode);

        if(null == bs){
            bs = new BasicSuggestion(original, suggestion, htmlSuggestion);
            WEAK_CACHE.put(hashCode, bs);

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
