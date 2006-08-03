/* Copyright (2005-2006) Schibsted Søk AS
 * RunningQuery.java
 *
 * Created on 16 February 2006, 19:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.query.run;

import java.util.List;
import java.util.Locale;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.searchportal.configuration.SearchMode;
import no.schibstedsok.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.view.config.SearchTab;

/** A RunningQuery is the central controller for a user's submitted search.
 * It has a one-to-one mapping to a search mode (see tabs.xml).
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface RunningQuery {

    public interface Context extends BaseContext, ResourceContext, SiteContext {
        /** TODO comment me. **/
    /** TODO comment me. **/
    SearchMode getSearchMode();
        /** TODO comment me. **/
    /** TODO comment me. **/
    /** TODO comment me. **/
    SearchTab getSearchTab();
    }

    /** TODO comment me. **/
    void addSource(final Modifier modifier);

    /** TODO comment me. **/
    List getEnrichments();

    /** TODO comment me. **/
    List getGeographicMatches();

    /**
     * First find out if the user types in an advanced search etc by analyzing the queryStr.
     * Then lookup correct tip using messageresources.
     *
     * @return user tip
     */
    String getGlobalSearchTips();

    /** TODO comment me. **/
    Locale getLocale();

    /** TODO comment me. **/
    Integer getNumberOfHits(final String configName);

    /** TODO comment me. **/
    int getNumberOfTerms();

    /** TODO comment me. **/
    Query getQuery();

    /** TODO comment me. **/
    String getQueryString();

    /** TODO comment me. **/
    SearchMode getSearchMode();

    /** TODO comment me. **/
    SearchTab getSearchTab();

    /** TODO comment me. **/
    List<Modifier> getSources();

    /** TODO comment me. **/
    TokenEvaluationEngine getTokenEvaluationEngine();

    /**
     * Thread run
     *
     * @throws InterruptedException
     */
    void run() throws InterruptedException;

}
