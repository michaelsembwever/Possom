/*
 * Copyright (2005) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.query.QueryTransformer;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.result.ResultHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchConfiguration {

    /**
     * Returns a list of {@link no.schibstedsok.front.searchportal.query.QueryTransformer} that should be applied to
     * the query before the query is sent to search indices.
     *
     * @return The list of query.
     */
    List getQueryTransformers();

    /**
     * Adds a {@link no.schibstedsok.front.searchportal.query.QueryTransformer} to the list of transformeres.
     *
     * @param transformer The query transformer to add.
     */
    void addQueryTransformer(QueryTransformer transformer);

    /**
     * Returns a list of {@link no.schibstedsok.front.searchportal.result.ResultHandler} that should act on the search
     * result.
     *
     * @return The list of handlers.
     */
    List getResultHandlers();

    /**
     * Adds a {@link no.schibstedsok.front.searchportal.result.ResultHandler} to the list of handlers.
     *
     * @param handler The handler to add.
     */
    void addResultHandler(ResultHandler handler);

    /**
     * Returns the name of this configuration.
     *
     * @return the name of the configuration.
     */
    String getName();

    /**
     * Returns the number of results to return.
     *
     * @return
     */
    int getResultsToReturn();

    /**
     * Returns true if paging shoud be enabled to this configuration. This
     * is typically only set to true for one of the configurations in a
     * {@link no.schibstedsok.front.searchportal.configuration.SearchMode}
     *
     * @return
     */
    boolean isPagingEnabled();

    /**
     * @return
     */

    Collection getResultFields();

    /**
     * @param resultField
     */
    void addResultField(String resultField);

    /**
     * Sets the number of results to return. This is typically set to the
     * page size.
     *
     * @param numberOfResults
     */
    void setResultsToReturn(int numberOfResults);

    boolean isChild();

    public String getRule();
    public int getRuleThreshold();
    public String getUseParameterAsQuery();

    boolean isAlwaysRunEnabled();
}
