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
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import org.w3c.dom.Element;

/**
 *  Spelling suggestions are chosen in the following way:
 *
 * <ul>
 * <li>Discard all suggestions with score less than minimumScore</li>
 * <li>For each term, remove suggestions with lowest score so that the number
 *     of suggestions is less than maxSuggestions. If the query is long the limit is maxSuggestionsForLongQueries</li>
 * <li>Remove all suggestions whose score differs more than maxDistance from the suggestion with the highest score</li>
 * <li>If the query is long and if two terms have suggestions, remove all suggestions unless the best suggestion
 * is much better than the second best</li>
 * </ul>
 *
 * A new query is then created using the chosen suggestions.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("SpellingSuggestionChooser")
public final class SpellingSuggestionChooserResultHandlerConfig extends AbstractResultHandlerConfig {

    private int minimumScore = -1;
    private int maxSuggestions = 3;
    private int maxDistance = 0;
    private int muchBetter = 5;
    private int maxSuggestionsForLongQueries = 2;
    private int longQuery = 2;
    private int veryLongQuery = 3;

    /**
     * Sets the minimum score a suggestions needs to have to be considered.
     *
     * @param minumScore New minimum score.
     */
    public void setMinScore(final int minumScore) {
        this.minimumScore = minumScore;
    }

    /**
     *
     * @return
     */
    public int getMinScore(){
        return minimumScore;
    }

    /**
     * Sets the maximum number of suggestions to choose.
     *
     * @param maxSuggestions New max number of suggestions.
     */
    public void setMaxSuggestions(final int maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
    }

    /**
     *
     * @return
     */
    public int getMaxSuggestions(){
        return maxSuggestions;
    }

    /**
     * Sets the maximum difference in score a suggestion can have from the highest
     * the suggestion with the highest score without being discarded.
     *
     * @param maxDistance New max distance.
     */
    public void setMaxDistance(final int maxDistance) {
        this.maxDistance = maxDistance;
    }

    /**
     *
     * @return
     */
    public int getMaxDistance(){
        return maxDistance;
    }

    /**
     * Sets the score difference needed for a suggestion to be rated as much better.
     *
     * @param muchBetter New difference.
     */
    public void setMuchBetter(final int muchBetter) {
        this.muchBetter = muchBetter;
    }

    /**
     *
     * @return
     */
    public int getMuchBetter(){
        return muchBetter;
    }

    /**
     * Sets the number of terms query needs to be considered as long.
     *
     * @param longQuery The new number of terms.
     */
    public void setLongQuery(final int longQuery) {
        this.longQuery = longQuery;
    }

    /**
     *
     * @return
     */
    public int getLongQuery(){
        return longQuery;
    }

    /**
     * Sets the number of terms a query needs to be considered as long.
     *
     * @param veryLongQuery The new number of terms.
     */
    public void setVeryLongQuery(final int veryLongQuery) {
        this.veryLongQuery = veryLongQuery;
    }

    /**
     *
     * @return
     */
    public int getVeryLongQuery(){
        return veryLongQuery;
    }

    /**
     * Sets the number of suggestions to choose for very long queries.
     *
     * @param maxSuggestionsForLongQueries The new number of suggestions.
     */
    public void setLongQueryMaxSuggestions(final int maxSuggestionsForLongQueries) {
        this.maxSuggestionsForLongQueries = maxSuggestionsForLongQueries;
    }

    /**
     *
     * @return
     */
    public int getLongQueryMaxSuggestions(){
        return maxSuggestionsForLongQueries;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);

        setMinScore(AbstractDocumentFactory.parseInt(element.getAttribute("min-score"), -1));
        setMaxSuggestions(AbstractDocumentFactory.parseInt(element.getAttribute("max-suggestions"), -1));
        setMaxDistance(AbstractDocumentFactory.parseInt(element.getAttribute("max-distance"), -1));
        setMuchBetter(AbstractDocumentFactory.parseInt(element.getAttribute("much-better"), -1));
        setLongQuery(AbstractDocumentFactory.parseInt(element.getAttribute("long-query"), -1));
        setVeryLongQuery(AbstractDocumentFactory.parseInt(element.getAttribute("very-long-query"), -1));
        setLongQueryMaxSuggestions(
                AbstractDocumentFactory.parseInt(element.getAttribute("long-query-max-suggestions"), -1));

        return this;
    }


}
