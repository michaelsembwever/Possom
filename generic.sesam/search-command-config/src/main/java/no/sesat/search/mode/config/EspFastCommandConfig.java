/* Copyright (2007-2008) Schibsted Søk AS
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
/*
 * AdvancedFastConfiguration.java
 *
 * Created on May 30, 2006, 4:16 PM
 *
 */

package no.sesat.search.mode.config;

import java.util.Collection;
import no.sesat.search.result.Navigator;

import java.util.HashMap;
import java.util.Map;
import no.sesat.search.mode.SearchModeFactory.Context;

import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @version $Id$
 */
@Controller("ESPFastSearchCommand")
public class EspFastCommandConfig extends FastCommandConfig {

    // Constants -----------------------------------------------------

    private String view = "";
    private String queryServer = "";
    private String sortBy = "default";
    private String alternativeSortBy = "";
    private boolean collapsingEnabled;
    private boolean expansionEnabled;
    private boolean collapsingRemoves;
    private String qtPipeline = "";
    private boolean lemmatize;
    private Integer timeout = 1000;


    private static final String ERR_FAST_EPS_QR_SERVER =
            "Query server address cannot contain the scheme (http://): ";


    /**
     *
     * @param collapsingEnabled
     */
    public void setCollapsingEnabled(final boolean collapsingEnabled) {
        this.collapsingEnabled = collapsingEnabled;
    }

    /**
     *
     * @return
     */
    public boolean isCollapsingEnabled() {
        return collapsingEnabled;
    }

    /**
     * Returns true if expansion is enabled. Expansion means the possibility
     * to retrieve all of the documents that has been collapsed for a domain. If
     * this is set to false the templates won't get the information that there
     * are collapsed documents.
     *
     * @return true if expansion is enabled.
     */
    public boolean isExpansionEnabled() {
        return expansionEnabled;
    }

    /**
     * Setter for the expansionEnabled property.
     *
     * @param expansionEnabled
     */
    public void setExpansionEnabled(final boolean expansionEnabled) {
        this.expansionEnabled = expansionEnabled;
    }

    /**
     *
     * @return
     */
    public boolean isCollapsingRemoves() {
        return collapsingRemoves;
    }

    /**
     *
     * @param collapsingRemoves
     */
    public void setCollapsingRemoves(final boolean collapsingRemoves) {
        this.collapsingRemoves = collapsingRemoves;
    }

    private final Map<String, Navigator> navigators = new HashMap<String,Navigator>();

    /**
     *
     */
    public EspFastCommandConfig() {
    }

    /**
     *
     * @return
     */
    public String getView() {
        return view;
    }

    /**
     *
     * @param view
     */
    public void setView(final String view) {
        this.view = view;
    }

    /**
     *
     * @return
     */
    public String getQueryServer() {
        return queryServer;
    }

    /**
     *
     * @param queryServer
     */
    public void setQueryServer(final String queryServer) {
        this.queryServer = queryServer;
    }

    /**
     *
     * @return
     */
    @Override
    public String getSortBy() {
        return sortBy;
    }

    /**
     *
     * @param sortBy
     */
    @Override
    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * This is used as an alternetive sort order that can be swiched to from the client
     * @return
     */
    public String getAlternativeSortBy() {
        return alternativeSortBy;
    }

    public void setAlternativeSortBy(String alternativeSortBy) {
        this.alternativeSortBy = alternativeSortBy;
    }

    /**
     *
     * @return
     */
    @Override
    public Map<String, Navigator> getNavigators() {
        return navigators;
    }
    /**
     *
     * @param navigator
     * @param navKey
     */
    @Override
    public void addNavigator(final Navigator navigator, final String navKey) {
        navigators.put(navKey, navigator);
    }

    /**
     *
     * @param navigatorKey
     * @return
     */
    @Override
    public Navigator getNavigator(final String navigatorKey) {
        return navigators.get(navigatorKey);
    }

    /**
     *
     * @param qtPipeline
     */
    @Override
    public void setQtPipeline(final String qtPipeline) {
        this.qtPipeline = qtPipeline;
    }

    /**
     *
     * @return
     */
    @Override
    public String getQtPipeline() {
        return qtPipeline;
    }

    /**
     *
     * @param lemmatize
     */
    public void setLemmatize(final boolean lemmatize) {
        this.lemmatize = lemmatize;
    }

    /**
     *
     * @return
     */
    public boolean isLemmatize() {
        return lemmatize;
    }

    /** Fast ESP supports timeout on query with BaseParameter.TIMEOUT.
     * Specified in milliseconds.
     * Default is one second.
     * Only actived when root log4j logger is set to INFO or higher.
     * Rationale here is that we don't want timeouts in debugging environments.
     * @param integer
     */
    public void setTimeout(final int integer){
        timeout = integer;
    }

    /** @see #setTimeout(java.lang.Integer)
     *
     * @return
     */
    public int getTimeout(){
        return timeout;
    }


    @Override
    public SearchConfiguration readSearchConfiguration(final Element element, final SearchConfiguration inherit, Context context) {
        super.readSearchConfiguration(element, inherit, context);
        final EspFastCommandConfig efscInherit = inherit instanceof EspFastCommandConfig ? (EspFastCommandConfig) inherit
                : null;


        if (null != getQueryServer() && getQueryServer().startsWith("http://")) {
            throw new IllegalArgumentException(ERR_FAST_EPS_QR_SERVER + getQueryServer());
        }

        if (efscInherit != null && efscInherit.getNavigators() != null) {

            navigators.putAll(efscInherit.getNavigators());
        }

        final NodeList nList = element.getElementsByTagName("navigators");

        for (int i = 0; i < nList.getLength(); ++i) {
            final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
            for (Navigator navigator : navigators) {
                addNavigator(navigator, navigator.getId());
            }
        }

        return this;
    }
}
