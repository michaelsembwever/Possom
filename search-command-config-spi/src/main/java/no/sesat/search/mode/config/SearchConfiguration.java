/*
 * Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package no.sesat.search.mode.config;

import java.io.Serializable;
import java.util.Map;
import no.sesat.search.mode.SearchModeFactory.Context;
import org.w3c.dom.Element;

/**
 *
 * @version $Id$
 */
public interface SearchConfiguration extends Serializable {

    /**
     * Default value associated with getPagingParameter()
     */
    public static final String DEFAULT_PAGING_PARAMETER = "offset";

    /**
     * Default value associated with getUserSortParameter()
     */
    public static final String DEFAULT_USER_SORT_PARAMETER = "sort";

    /**
     * @param resultField
     */
    void addResultField(String... resultField);

    /**
     * Getter for property fieldFilters.
     *
     * @return Value of property fieldFilters.
     */
    Map<String, String> getFieldFilterMap();

    /**
     * Remove all fieldFilters.
     */
    void clearFieldFilters();

    /**
     * Returns the name of this configuration.
     *
     * @return the name of the configuration.
     */
    String getId();

    String getName();

    /**
     * @return
     */
    String getQueryParameter();

    /**
     * @return
     */
    Map<String, String> getResultFieldMap();

    /**
     * Returns the number of results to return.
     *
     * @return
     */
    int getResultsToReturn();

    /**
     * @return The statistical name.
     */
    String getStatisticalName();

    /** Return the parameter key that any associated ResultPagingNavigationController
     * should use to fetch the offset value.
     * Defaults to "offset".
     *
     * Typically used when multiple ResultPagingNavigationControllers are to be configured on the one mode.
     *
     * @return
     */
    String getPagingParameter();

    /** Return the parameter key that any associated PagingNavigationController for sorting
     * should use to fetch the offset value.
     * Defaults to "sort".
     *
     * Typically used when multiple sorting PagingNavigationControllers are to be configured on the one mode.
     *
     * @return the userSortParameter value
     */
    public String getUserSortParameter();

    /**
     * @return true if the command should always run.
     */
    boolean isAlwaysRun();

    /**
     * Is the command used asynchronously, for example by ajax calls.
     * @return
     */
    boolean isAsynchronous();

    /**
     * @return true if the command should run when query string is blank.
     */
    boolean isRunBlank();

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
         * @return The newly read configuration (chaining pattern)
         */
        SearchConfiguration readSearchConfiguration(Element element, SearchConfiguration inherit, Context context);
    }
}


