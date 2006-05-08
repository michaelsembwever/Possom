/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastConfiguration extends AbstractSearchConfiguration {

    private final List collections = new ArrayList();
    private Map searchParameters;
    private boolean lemmatizeEnabled;
    private boolean spellcheckEnabled;
    private final Map navigators = new HashMap();
    private String sortBy;
    private boolean collapsingEnabled;
    private String queryServerURL;
    private boolean keywordClusteringEnabled = false;
    private String qtPipeline;
    private volatile transient String collectionString;

    private String resultView;
    private boolean clusteringEnabled = false;
    private boolean ignoreNavigationEnabled = false;
    private int offensiveScoreLimit = 0;
    private int spamScoreLimit = 0;

    private String filter;

    private boolean relevantQueriesEnabled = false;

    public FastConfiguration(){
        super(null);
    }

    public FastConfiguration(final SearchConfiguration asc){

        super(asc);
        if(asc != null && asc instanceof FastConfiguration){
            final FastConfiguration fsc = (FastConfiguration) asc;
            collections.addAll(fsc.collections);
            searchParameters = fsc.searchParameters;
            lemmatizeEnabled = fsc.lemmatizeEnabled;
            spellcheckEnabled = fsc.spellcheckEnabled;
            navigators.putAll(fsc.navigators);
            sortBy = fsc.sortBy;
            collapsingEnabled = fsc.collapsingEnabled;
            queryServerURL = fsc.queryServerURL;
            keywordClusteringEnabled = fsc.keywordClusteringEnabled;
            qtPipeline = fsc.qtPipeline;
            collectionString = fsc.collectionString;
            resultView = fsc.resultView;
            clusteringEnabled = fsc.clusteringEnabled;
            ignoreNavigationEnabled = fsc.ignoreNavigationEnabled;
            offensiveScoreLimit = fsc.offensiveScoreLimit;
            spamScoreLimit = fsc.spamScoreLimit;
            filter = fsc.filter;
            relevantQueriesEnabled = fsc.relevantQueriesEnabled;
        }
    }

    public List getCollections() {
        return collections;
    }

    public void addCollection(final String collectionName) {
        collections.add(collectionName);
    }

    /**
     * Double-checked locking idiom will not work without the volatile keyword and JAVA 5.
     * (Not a big performance difference to simply making the method synchronized).
     * See Effective Java -> Item 48: Synchronize access to shared mutable data
     **/
    public String getCollectionFilterString() {
        if (collectionString == null) {
            synchronized (this) {
                if (collectionString == null) {
                    collectionString = generateFilterString();
                }
            }
        }

        return collectionString;
    }

    private String generateFilterString() {

        if (collections != null) {
            if (collections.size() > 1) {
             final Collection invertedCollection = new ArrayList();
                for (int i = 0; i < SearchConstants.ALL_COLLECTIONS.length; i++) {
                    final String c = SearchConstants.ALL_COLLECTIONS[i];
                    invertedCollection.add(c);
                }

                invertedCollection.removeAll(collections);

                final Object [] coll = prependMetaCollection(invertedCollection);
                return StringUtils.join(coll, ' ');
            } else if (collections.size() == 1) {
                return "+meta.collection:" + collections.get(0);
            }
        }
        return "";
    }

    private Object[] prependMetaCollection(final Collection collectionStrings) {
        final Object coll[] =  collectionStrings.toArray();

        for (int i = 0; i < coll.length; i++) {
            final String collectionName = (String) coll[i];
            final String s = "-meta.collection:" + collectionName;
            coll[i] = s;
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

    public boolean isLemmatizeEnabled() {
        return lemmatizeEnabled;
    }

    public void setLemmatizeEnabled(final boolean lemmatizeEnabled) {
        this.lemmatizeEnabled = lemmatizeEnabled;
    }

    public boolean isSpellcheckEnabled() {
        return spellcheckEnabled;
    }

    public void setSpellcheckEnabled(final boolean spellcheckEnabled) {
        this.spellcheckEnabled = spellcheckEnabled;
    }

    public Map getNavigators() {
        return navigators;
    }

//    public void setNavigators(Map navigators) {
//        this.navigators = navigators;
//    }

    public void addNavigator(final FastNavigator navigator, final String navKey) {
        navigators.put(navKey, navigator);
    }

    public FastNavigator getNavigator(final String navigatorKey) {
        return (FastNavigator) navigators.get(navigatorKey);
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isCollapsingEnabled() {
        return collapsingEnabled;
    }

    public void setCollapsingEnabled(final boolean collapsingEnabled) {
        this.collapsingEnabled = collapsingEnabled;
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

    public boolean isClusteringEnabled() {
        return clusteringEnabled;
    }

    public boolean isIgnoreNavigationEnabled() {
        return ignoreNavigationEnabled;
    }

    public void setIgnoreNavigationEnabled(final boolean ignoreNavigationEnabled) {
        this.ignoreNavigationEnabled = ignoreNavigationEnabled;
    }

    public int getOffensiveScoreLimit() {
        return offensiveScoreLimit;
    }

    public int getSpamScoreLimit() {
        return spamScoreLimit;
    }

    public boolean isRelevantQueriesEnabled() {
        return relevantQueriesEnabled;
    }

    public String getFilter() {
        return filter;
    }

    void setSpamScoreLimit(final int i) {
        spamScoreLimit = i;
    }

    /**
     * Setter for property clusteringEnabled.
     * @param clusteringEnabled New value of property clusteringEnabled.
     */
    public void setClusteringEnabled(final boolean clusteringEnabled) {
        this.clusteringEnabled = clusteringEnabled;
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
     * Setter for property relevantQueriesEnabled.
     * @param relevantQueriesEnabled New value of property relevantQueriesEnabled.
     */
    public void setRelevantQueriesEnabled(final boolean relevantQueriesEnabled) {
        this.relevantQueriesEnabled = relevantQueriesEnabled;
    }
}
