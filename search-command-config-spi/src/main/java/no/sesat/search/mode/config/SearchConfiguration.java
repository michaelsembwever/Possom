/*
 * Copyright (2005-2008) Schibsted Søk AS
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
 *
 */
package no.sesat.search.mode.config;

import java.io.Serializable;
import no.sesat.search.query.transform.QueryTransformerConfig;
import no.sesat.search.result.handler.ResultHandlerConfig;

import java.util.List;
import java.util.Map;
import no.sesat.search.mode.SearchModeFactory.Context;
import org.w3c.dom.Element;

/** Minimum behavior defined for any SearchConfiguration implementation.
 *
 *
 * @version <tt>$Id$</tt>
 */
public interface SearchConfiguration extends Serializable {
    /**
     * Returns a (defensive copy) list of {@link no.sesat.search.query.transform.QueryTransformerConfig} that should be applied to
     * the query before the query is sent to search indices.
     *
     * @return The list of query.
     */
    List<QueryTransformerConfig> getQueryTransformers();

    /**
     * Adds a {@link no.sesat.search.query.transform.QueryTransformerConfig} to the list of transformeres.
     *
     * @param transformer The query transformer to add.
     */
    void addQueryTransformer(QueryTransformerConfig transformer);

    /**
     * Returns a (defensive copy) list of {@link no.sesat.search.result.handler.ResultHandlerConfig} that should act on the search
     * result.
     *
     * @return The list of handlers.
     */
    List<ResultHandlerConfig> getResultHandlers();

    /**
     * Adds a {@link no.sesat.search.result.handler.ResultHandlerConfig} to the list of handlers.
     *
     * @param handler The handler to add.
     */
    void addResultHandler(ResultHandlerConfig handler);

    /**
     * Returns the name of this configuration.
     *
     * @return the name of the configuration.
     */
    String getId();

    String getName();

    /**
     * Returns the number of results to return.
     *
     * @return
     */
    int getResultsToReturn();

    /**
     * @return
     */
    Map<String,String> getResultFieldMap();

    /**
     * @param resultField
     */
    void addResultField(String... resultField);

    /**
     * Sets the number of results to return. This is typically set to the
     * page size defined in the view.xml.
     *
     * @param numberOfResults
     */
    void setResultsToReturn(int numberOfResults);

    /**
     * @return
     */
    public String getQueryParameter();

    /**
     * @return true if the command should always run.
     */
    boolean isAlwaysRun();

    /**
     * @return true if the command should run when query string is blank.
     */
    boolean isRunBlank();

    /**
     * @return The statistical name.
     */
    String getStatisticalName();

    /**
     * Getter for property fieldFilters.
     *
     * @return Value of property fieldFilters.
     */
    Map<String, String> getFieldFilterMap();

    /**
     * Clear all query transformers associated with this configuration.
     */
    void clearQueryTransformers();

    /**
     * Clear all result handlers associated with this configuration.
     */
    void clearResultHandlers();

    /**
     * Removes all field filters associated with this configuration.
     */
    void clearFieldFilters();

    /**
     * Is the command used asynchronously, for example by ajax calls.
     * @return
     */
    boolean isAsynchronous();

    /**
     * Interface for SearchConfigurations that uses W3cDomDeserialiser.
     *
     */
    public interface ModesW3cDomDeserialiser extends SearchConfiguration{

        /**
         * Apply the attributes found in element to 'this'. If some attributes are not found
         * in element then try to fetch them from inherit and set them on 'this'.
         *
         * @param element
         * @param inherit
         *
         * @return The newly read configuration (Done to keep the chaining pattern)
         */
        SearchConfiguration readSearchConfiguration(Element element, SearchConfiguration inherit, Context context);
    }
}


