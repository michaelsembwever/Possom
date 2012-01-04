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
package no.sesat.search.mode;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.run.handler.RunHandlerConfig;
import no.sesat.search.run.transform.RunTransformerConfig;

/**
 *
 * @version <tt>$Id$</tt>
 */
public final class SearchMode implements Serializable {

    // Constants -----------------------------------------------------

    /**
     *
     */
    public enum SearchCommandExecutorConfig{
        /**
         *
         */
        @Controller("SequentialSearchCommandExecutor")
        SEQUENTIAL,
        /**
         *
         */
        @Controller("ThrottledSearchCommandExecutor")
        PARALLEL;
        /**
         *
         */
        @Documented
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.FIELD})
        @Inherited
        public @interface Controller {
            /**
             *
             * @return
             */
            public String value();
        }
    }

    // Attributes ----------------------------------------------------

    private SearchCommandExecutorConfig searchCommandExecutor = SearchCommandExecutorConfig.SEQUENTIAL;

    private Collection<SearchConfiguration> searchConfigurations;
    private SearchMode parentSearchMode;
    private boolean queryEvaluationEnabled = true;
    private boolean queryAnalysisEnabled = false;
    private boolean autoBroadening = true;
    private String parentMode;
    private String id;
    private List<RunHandlerConfig> runHandlers;
    private List<RunTransformerConfig> runTransformers;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     *
     */
    public SearchMode(){
    }

    /**
     *
     * @param inherit
     */
    public SearchMode(final SearchMode inherit){
        if( inherit != null ){
            parentSearchMode = inherit;
            parentMode = inherit.parentMode;
            queryEvaluationEnabled = inherit.queryEvaluationEnabled;
            queryAnalysisEnabled = inherit.queryAnalysisEnabled;
            searchCommandExecutor = inherit.searchCommandExecutor;
            autoBroadening = inherit.autoBroadening;
        }
    }

    /**
     *
     * @return
     */
    public Collection<SearchConfiguration> getSearchConfigurations() {
        return searchConfigurations;
    }

    /**
     *
     * @param name
     * @return
     */
    public SearchConfiguration getSearchConfiguration(final String name) {

        for( SearchConfiguration sc : searchConfigurations){
            if( sc.getId().equals(name) ){
                return sc;
            }
        }
        return null;
    }

    /**
     *
     * @param searchConfigurations
     */
    public void setSearchConfigurations(Collection<SearchConfiguration> searchConfigurations) {
        this.searchConfigurations = Collections.unmodifiableCollection(searchConfigurations);
    }

    /**
     *
     * @return
     */
    public SearchCommandExecutorConfig getExecutor() {
        return searchCommandExecutor;
    }

    /**
     *
     * @param searchCommandExecutor
     */
    public void setExecutor(final SearchCommandExecutorConfig searchCommandExecutor) {
        this.searchCommandExecutor = searchCommandExecutor;
    }

    /**
     *
     * @return
     */
    public SearchMode getParentSearchMode() {
        return parentSearchMode;
    }

    /**
     * Get the autoBroadening.
     * Automatic broadening results in any query returning zero hits across all commands will be wrapped in
     *  parenthesis () and re-executed. This changes any DefaultOperatorClauses to OrClauses.
     *
     * @return the autoBroadening.
     */
    public boolean isAutoBroadening() {
        return autoBroadening;
    }

    /**
     * Set the autoBroadening.
     *
     * @param autoBroadening The autoBroadening to set.
     */
    public void setAutoBroadening(boolean autoBroadening) {
        this.autoBroadening = autoBroadening;
    }

    /**
     * Get the queryAnalysisEnabled.
     * To return true isEvaluation() must also return true.
     *
     * @return the queryAnalysisEnabled.
     */
    public boolean isAnalysis() {
        return queryEvaluationEnabled && queryAnalysisEnabled;
    }

    /**
     * Set the queryAnalysisEnabled.
     *
     * @param queryAnalysisEnabled The queryAnalysisEnabled to set.
     */
    public void setAnalysis(boolean queryAnalysisEnabled) {
        this.queryAnalysisEnabled = queryAnalysisEnabled;
    }

    /**
     * Get the queryEvaluationEnabled.
     *
     * @return the queryEvaluationEnabled.
     */
    public boolean isEvaluation() {
        return queryEvaluationEnabled;
    }

    /**
     * Set the queryEvaluationEnabled.
     *
     * @param queryEvaluationEnabled The queryEvaluationEnabled to set.
     */
    public void setEvaluation(boolean queryEvaluationEnabled) {
        this.queryEvaluationEnabled = queryEvaluationEnabled;
    }

    /**
     * Getter for property parentMode.
     * @return Value of property parentMode.
     */
    public String getParentMode() {
        return this.parentMode;
    }

    /**
     * Setter for property parentMode.
     * @param parentMode New value of property parentMode.
     */
    public void setParentMode(String parentMode) {
        this.parentMode = parentMode;
    }

    @Override
    public String toString(){
        return id + (parentSearchMode != null ? " --> " + parentSearchMode.toString() : "");
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Setter for run handlers
     * @param runHandlers New list of run handlers
     */
    public void setRunHandlers(final List<RunHandlerConfig> runHandlers) {
        this.runHandlers = Collections.unmodifiableList(runHandlers);
    }

    /**
     * Getter for run handlers
     * @return List of run handlers
     */
     public List<RunHandlerConfig> getRunHandlers() {
         return runHandlers;
     }

     /**
      * Setter for run transformers
      * @param runTransformers New List of run transformers
      */
     public void setRunTransformers(final List<RunTransformerConfig> runTransformers) {
         this.runTransformers = Collections.unmodifiableList(runTransformers);
     }

     /**
      * Getter for run transformers
      * @return List of run transformers
      */
     public List<RunTransformerConfig> getRunTransformers() {
         return runTransformers;
     }

    // Inner classes -------------------------------------------------

}
