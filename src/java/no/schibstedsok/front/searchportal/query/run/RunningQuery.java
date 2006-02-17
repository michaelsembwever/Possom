/* Copyright (2005-2006) Schibsted Søk AS
 * RunningQuery.java
 *
 * Created on 16 February 2006, 19:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.run;

import java.util.List;
import java.util.Locale;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.site.SiteContext;

/** A RunningQuery is the central controller for a user's submitted search.
 * It has a one-to-one mapping to a search mode (see tabs.xml).
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface RunningQuery {

    public interface Context extends ResourceContext, SiteContext {
        SearchMode getSearchMode();
    }

    void addSource(final Modifier modifier);

    List getEnrichments();

    List getGeographicMatches();

    /**
     * First find out if the user types in an advanced search etc by analyzing the queryStr.
     * Then lookup correct tip using messageresources.
     *
     * @return user tip
     */
    String getGlobalSearchTips();

    Locale getLocale();

    Integer getNumberOfHits(final String configName);

    int getNumberOfTerms();

    int getOffset();
    
    Query getQuery();

    String getQueryString();

    SearchMode getSearchMode();

    String getSourceParameters(final String source);

    List getSources();

    /**
     * Get the strippedQueryString.
     *
     * @return the strippedQueryString.
     */
    String getStrippedQueryString();

    TokenEvaluatorFactory getTokenEvaluatorFactory();

    /**
     * Thread run
     *
     * @throws InterruptedException
     */
    void run() throws InterruptedException;

    void setOffset(final int offset);

}
