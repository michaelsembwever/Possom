/*
 * Copyright (2005-2008) Schibsted ASA
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

import no.sesat.search.query.transform.QueryTransformerConfig;
import no.sesat.search.result.handler.ResultHandlerConfig;

import java.util.List;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;

/** Minimum behavior defined for any AbstractSearchConfiguration implementation.
 *
 * @version <tt>$Id$</tt>
 */
public interface BaseSearchConfiguration extends SearchConfiguration {

    void clearQueryTransformers();

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
     *  Remove all result handlers.
     */
    void clearResultHandlers();


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

    QueryBuilderConfig getQueryBuilder();

    void setQueryBuilder(QueryBuilderConfig queryBuilderConfig);

    QueryTransformerConfig getInitialQueryTransformer();

    void setInitialQueryTransformer(QueryTransformerConfig initialQueryTransformer);
}
