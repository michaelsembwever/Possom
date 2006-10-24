/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.searchportal.mode.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.result.Navigator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastSearchConfiguration extends AbstractSearchConfiguration {
    
    private static final Logger LOG = Logger.getLogger(FastSearchConfiguration.class);
    
    private static final String[] ALL_COLLECTIONS = {
        "tv",
        "webcrawlno1",
        "webcrawlno1",
        "webcrawlno1deep1",
        "webcrawlno2",
        "wikipedia",
        "wikipedia2",
        "WikipediaSV",
        "robots",
        "yellow",
        "white",
        "weather",
        "carelscrawl",
        "retriever",
        "moreover",
        "retrievernordic",
        "mano",
        "skiinfo"
    };

    private final List<String> collections = new ArrayList<String>();
    private Map searchParameters;
    private boolean lemmatise;
    private boolean spellcheck;
    private final Map<String, Navigator> navigators = new HashMap<String,Navigator>();
    private String sortBy;
    private boolean collapsing;
    private String queryServerURL;
    private boolean keywordClusteringEnabled = false;
    private String qtPipeline;
    private transient volatile String collectionString;
    private boolean expansionEnabled;
    
    private String resultView;
    private boolean clustering = false;
    private boolean ignoreNavigation = false;
    private int offensiveScoreLimit = 0;
    private int spamScoreLimit = 0;

    private String filter;

    private boolean relevantQueries = false;

    public FastSearchConfiguration(){
        super(null);
    }

    public FastSearchConfiguration(final SearchConfiguration asc){

        super(asc);
        if(asc != null && asc instanceof FastSearchConfiguration){
            final FastSearchConfiguration fsc = (FastSearchConfiguration) asc;
            collections.addAll(fsc.collections);
            searchParameters = fsc.searchParameters;
            lemmatise = fsc.lemmatise;
            spellcheck = fsc.spellcheck;
            navigators.putAll(fsc.navigators);
            sortBy = fsc.sortBy;
            collapsing = fsc.collapsing;
            queryServerURL = fsc.queryServerURL;
            keywordClusteringEnabled = fsc.keywordClusteringEnabled;
            qtPipeline = fsc.qtPipeline;
            collectionString = fsc.collectionString;
            resultView = fsc.resultView;
            clustering = fsc.clustering;
            ignoreNavigation = fsc.ignoreNavigation;
            offensiveScoreLimit = fsc.offensiveScoreLimit;
            spamScoreLimit = fsc.spamScoreLimit;
            filter = fsc.filter;
            relevantQueries = fsc.relevantQueries;
        }
    }

    public List<String> getCollections() {
        return collections;
    }

    public void addCollection(final String collectionName) {
        collections.add(collectionName);
    }

    public String getCollectionFilterString() {
        if (collectionString == null) {
            collectionString = createCollectionFilterString();
        }

        return collectionString;
    }

    private String createCollectionFilterString() {
        
        String result = "";
        
        if (collections.size() > 1) {

            final Collection invertedCollection = new ArrayList(Arrays.asList(ALL_COLLECTIONS));
            invertedCollection.removeAll(collections);
            final String [] coll = prependMetaCollection(invertedCollection);
            result = StringUtils.join(coll, ' ');

        } else if (collections.size() == 1) {
            result = "+meta.collection:" + collections.get(0);
        }
        return result;
    }

    private String[] prependMetaCollection(final Collection<String> collectionStrings) {
        
        final String coll[] = collectionStrings.toArray(new String[collectionStrings.size()]);

        for (int i = 0; i < coll.length; i++) {
            coll[i] = "-meta.collection:" + coll[i];
        }
        return coll;
    }

    public String getQueryServerURL() {
        return queryServerURL;
    }

    public Map getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(final Map searchParameters) {
        this.searchParameters = searchParameters;
    }

    public void setParameter(final String parameterName, final Object parameterValue) {
        searchParameters.put(parameterName, parameterValue);
    }

    public boolean isLemmatise() {
        return lemmatise;
    }

    public void setLemmatise(final boolean lemmatise) {
        this.lemmatise = lemmatise;
    }

    public boolean isSpellcheck() {
        return spellcheck;
    }

    public void setSpellcheck(final boolean spellcheckEnabled) {
        this.spellcheck = spellcheckEnabled;
    }

    public Map<String, Navigator> getNavigators() {
        return navigators;
    }

    public void addNavigator(final Navigator navigator, final String navKey) {
        navigators.put(navKey, navigator);
    }

    public Navigator getNavigator(final String navigatorKey) {
        return navigators.get(navigatorKey);
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isCollapsing() {
        return collapsing;
    }

    public void setCollapsing(final boolean collapsingEnabled) {
        this.collapsing = collapsingEnabled;
    }

    public String getResultView() {
        return resultView;
    }

    public void setResultView(final String resultView) {
        this.resultView = resultView;
    }

    public void setQueryServerURL(final String queryServerURL) {
        this.queryServerURL = queryServerURL;
    }

    public boolean isKeywordClusteringEnabled() {
        return keywordClusteringEnabled;
    }

    public void setKeywordClusteringEnabled(final boolean keywordClusteringEnabled) {
        this.keywordClusteringEnabled = keywordClusteringEnabled;
    }

    public String getQtPipeline() {
        return qtPipeline;
    }

    public void setQtPipeline(final String qtPipeline) {
        this.qtPipeline = qtPipeline;
    }

    public boolean isClustering() {
        return clustering;
    }

    public boolean isIgnoreNavigation() {
        return ignoreNavigation;
    }

    public void setIgnoreNavigation(final boolean ignoreNavigationEnabled) {
        this.ignoreNavigation = ignoreNavigationEnabled;
    }

    public int getOffensiveScoreLimit() {
        return offensiveScoreLimit;
    }

    public int getSpamScoreLimit() {
        return spamScoreLimit;
    }

    public boolean isRelevantQueries() {
        return relevantQueries;
    }

    public String getFilter() {
        return filter;
    }

    void setSpamScoreLimit(final int i) {
        spamScoreLimit = i;
    }

    /**
     * Setter for property clustering.
     * 
     * @param clustering New value of property clustering.
     */
    public void setClustering(final boolean clusteringEnabled) {
        this.clustering = clusteringEnabled;
    }

    /**
     * Setter for property collectionFilterString.
     * @param collectionFilterString New value of property collectionFilterString.
     */
    public void setCollectionFilterString(final String collectionFilterString) {
        this.collectionString = collectionString;
    }

    /**
     * Setter for property filter.
     * @param filter New value of property filter.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * Setter for property offensiveScoreLimit.
     * @param offensiveScoreLimit New value of property offensiveScoreLimit.
     */
    public void setOffensiveScoreLimit(final int offensiveScoreLimit) {
        this.offensiveScoreLimit = offensiveScoreLimit;
    }

    /**
     * Setter for property relevantQueries.
     * 
     * @param relevantQueries New value of property relevantQueries.
     */
    public void setRelevantQueries(final boolean relevantQueriesEnabled) {
        this.relevantQueries = relevantQueriesEnabled;
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
}
