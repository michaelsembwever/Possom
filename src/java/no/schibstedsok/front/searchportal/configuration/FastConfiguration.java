/*
 * Copyright (2005) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.command.FastSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.*;

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


    private boolean synonymsEnabled = false;

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
            synchronized(this) {
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
             Collection invertedCollection = new ArrayList();
                for (int i = 0; i < SearchConstants.ALL_COLLECTIONS.length; i++) {
                    String c = SearchConstants.ALL_COLLECTIONS[i];
                    invertedCollection.add(c);
                }

                invertedCollection.removeAll(collections);

                Object [] coll = prependMetaCollection(invertedCollection);
                return StringUtils.join(coll, ' ');
            } else if (collections.size() == 1) {
                return "+meta.collection:" + collections.get(0);
            }
        }
        return "";
    }

    private Object[] prependMetaCollection(final Collection collectionStrings) {
        Object coll[] =  collectionStrings.toArray();

        for (int i = 0; i < coll.length; i++) {
            String collectionName = (String) coll[i];
            String s = "-meta.collection:" + collectionName;
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

    public String toString() {                                            
        return ToStringBuilder.reflectionToString(this);
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
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

    public boolean isSynonymsEnabled() {
        return synonymsEnabled;
    }
}
