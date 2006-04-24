/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.result;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.result.handler.PhoneNumberChooser;
import no.schibstedsok.front.searchportal.result.handler.PhoneNumberFormatter;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.view.config.SearchTab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import no.schibstedsok.front.searchportal.command.SearchCommand;

public class YellowSearchResult extends FastSearchResult {

    private int pseudoLocalHitCount;
    Collection pseudoLocalResults = new ArrayList();
    private FastSearchResult localResult;
    private FastSearchResult pseudoLocalResult;
    private boolean local;
    private FastSearchResult top3;
    private int addedTop3 = 0;

    private static Log log = LogFactory.getLog(YellowSearchResult.class);

    public YellowSearchResult(final SearchCommand command, final FastSearchResult localResult, final FastSearchResult pseudoLocalResult, final FastSearchResult top3, final boolean local) {
        super(command);
        this.localResult = localResult;
        this.pseudoLocalResult = pseudoLocalResult;
        this.top3 = top3;
        this.local = local;

        List resultToAlter = getResults();

        List resultsToAdd = new ArrayList();

        for (Iterator iter = top3.getResults().iterator(); iter.hasNext();) {
            SearchResultItem item = (SearchResultItem) iter.next();

            if (Integer.parseInt(item.getField("rank")) > 100000) {
                resultsToAdd.add(item);
                ++addedTop3;

            }
        }

        resultToAlter.addAll(0, resultsToAdd);

        while (resultToAlter.size() > 10) {
            resultToAlter.remove(resultToAlter.size() - 1);
        }
    }

    private FastSearchResult getDelegator() {
        if (local) {
            return localResult;
        } else {
            return pseudoLocalResult;
        }
    }

    private FastSearchResult getOtherDelegator() {
        if (!local) {
            return localResult;
        } else {
            return pseudoLocalResult;
        }
    }

    public void setPseudoLocalHitCount(final int hitCount) {
        this.pseudoLocalHitCount = hitCount;
    }

    /**
     * Get the pseudoLocalHitCount.
     *
     * @return the pseudoLocalHitCount.
     */
    public int getPseudoLocalHitCount() {
        return pseudoLocalHitCount;
    }

    public Collection getPseudoLocalResults() {
        return pseudoLocalResults;
    }

    public void addPseudoLocalResult(final SearchResultItem item) {
        pseudoLocalResults.add(item);
    }

    public List getResults() {
        return getDelegator().getResults();
    }

    public Modifier getModifier(final String navigatorName, final String modifierName) {
        return getDelegator().getModifier(navigatorName, modifierName);
    }

    public int getModifierCount(final String navigatorName, final String modifierName) {
        return getDelegator().getModifierCount(navigatorName, modifierName);
    }

    public List getModifiers(final String navigatorName) {
        return getDelegator().getModifiers(navigatorName);
    }

    public int getHitCount() {
        return getDelegator().getHitCount() + addedTop3;
    }

    public FastSearchResult getLocalResult() {
        return localResult;
    }

    public int getSecondaryHitCount() {
        return getOtherDelegator().getHitCount();
    }

    public int getAllHitCount() {
        int hits = 0;

        if (localResult != null) {
            hits += localResult.hitCount;
        }

        if (pseudoLocalResult != null) {
            hits += pseudoLocalResult.hitCount;
        }

	hits += addedTop3;

        return hits;
    }

    public Collection getSecondaryHits() {

        final ResultHandler.Context resultHandlerContext = new ResultHandler.Context() {
                public SearchResult getSearchResult() {
                    return pseudoLocalResult;
                }

                public Site getSite() {
                    return Site.DEFAULT; // FIXME !!! Needs to work on a per SiteSearch basis.
                }

                public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                    return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
                }

                public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                    return UrlResourceLoader.newDocumentLoader(this, resource, builder);
                }

                public String getQueryString() {
                    return getSearchCommand().getRunningQuery().getQueryString();
                }

                public Query getQuery() {
                    return getSearchCommand().getRunningQuery().getQuery();
                }

                public void addSource(final Modifier modifier) {
                    getSearchCommand().getRunningQuery().addSource(modifier);
                }
                public SearchTab getSearchTab(){
                    return getSearchCommand().getRunningQuery().getSearchTab();
                }
            };

        PhoneNumberChooser chooser = new PhoneNumberChooser();
        chooser.handleResult(resultHandlerContext, null);

        PhoneNumberFormatter formatter = new PhoneNumberFormatter();
        formatter.handleResult(resultHandlerContext, null);

        Iterator pResults = pseudoLocalResult.getResults().iterator();

        Collection secondaryHits = new ArrayList();

        while (pResults.hasNext() && localResult.getHitCount() + secondaryHits.size() < 10) {
            SearchResultItem item = (SearchResultItem) pResults.next();

            if (!localResult.getResults().contains(item)) {
                secondaryHits.add(item);
            }
        }


        return secondaryHits;
    }

    /**
     * Get the local.
     *
     * @return the local.
     */
    public boolean isLocal() {
        return local;
    }
}
