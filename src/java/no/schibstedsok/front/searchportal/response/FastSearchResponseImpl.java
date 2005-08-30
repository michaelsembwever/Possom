/**
 * 
 */
package no.schibstedsok.front.searchportal.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import no.fast.ds.search.IModifier;

/**
 *
 * FAST specific response object that holds information
 * regarding search Time and spelling suggestions
 *
 * @author Lars Johansson
 *
 *
 */
public class FastSearchResponseImpl extends SearchResponseImpl {

    // set spelling suggestion from FAST if available in the Command
    private List spellingSuggestions = null;

    /**  the wikipedia result if existing*/
    private List wikiResult = new ArrayList();

    /**  the retriever results if existing in reponse */
    private List retrieverResults = new ArrayList();

    /** How many wiki results there was in index*/
    private int wikiDocumentsInIndex;

    /** How many media results there was in index*/
    private int mediaDocumentsInIndex;

    /** How many webcrawl results there was in index*/
    private int webCrawlDocumentsInIndex;

    /** The company results */
    private List companiesResults = new ArrayList();
    private List personsResults = new ArrayList();

    private List categoryModifiers = new ArrayList();
    private String modifier;

    private List moreoverResults = new ArrayList();
    private List nordicNewsResults = new ArrayList();

    public int getCompaniesDocumentsInIndex() {
        return companiesDocumentsInIndex;
    }

    /** How many companies are there in the search results */
    private int companiesDocumentsInIndex;


    /**
     *
     * Default empty Constructor
     */
    public FastSearchResponseImpl() {
        super();
    }

    /**
     * Add a Wiki result to the <code>wikiResult</code> <code>Collection</code>.
     *
     * @param wikiResult <code>SearchResultElement</code>
     */
    public void addWikiResult(SearchResultElement wikiResult){
        this.wikiResult.add(wikiResult);
    }

    /**
     *
     * Add a Wiki result to the <code>wikiResult</code> <code>Collection</code>.
     *
     * @param retrieverResult <code>SearchResultElement</code>
     */
    public void addRetreiverResult(SearchResultElement retrieverResult){
        this.retrieverResults.add(retrieverResult);
    }

    /**
     *
     *
     *
     * @param result
     */
    public void addCompaniesResult(SearchResultElement result) {
       companiesResults.add(result);
    }
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this).append("spellingSuggestions",
                this.spellingSuggestions).append("documentsReturned",
                this.getDocumentsReturned()).append("fetchTime",
                this.getFetchTime()).append("result", this.getResults()).append(
                "totalDocumentsAvailable", this.getTotalDocumentsAvailable()).toString();
    }

    /**
     *
     * Returns a <code>Collection</code> of <code>String</code> spelling suggstions
     *
     * @return
     */
    public Collection getSpellingSuggestions() {
        return spellingSuggestions;
    }


    /**
     *
     * Add a spelling suggestion to the <code>Collection</code>
     *
     * @param spellingSuggestion
     */
    public void addSpellingSuggestion(String spellingSuggestion) {
        if (! spellingSuggestion.equals(getQuery())) {
            if(this.spellingSuggestions == null)
                this.spellingSuggestions = new ArrayList();

            this.spellingSuggestions.add(spellingSuggestion);
        }
    }


    /**
     *
     * Returns a <code>Collection</code> of <code>SearchResultElement</code>
     * containing Media results
     *
     * @return <code>Collection</code>
     */
    public Collection getRetrieverResults() {
        Collections.sort(retrieverResults);
        return retrieverResults;
    }


    /**
     *
     * Returns a <code>Collection</code> of <code>SearchResultElement</code>
     * containing wiki results
     *
     * @return <code>Collection</code>
     */
    public Collection getWikiResult() {
        return wikiResult;
    }

    public List getCompaniesResults() {
        return companiesResults;
    }

    public void setCompaniesResults(List companiesResults) {
        this.companiesResults = companiesResults;
    }

    /** FIXME Comment this
     *
     * @param count
     */
    public void setWikiDocumentsReturned(int count) {
        this.wikiDocumentsInIndex = count;

    }

    /**
     *
     * How many media documents there was in index
     *
     * @return
     */
    public int getMediaDocumentsInIndex() {
        return mediaDocumentsInIndex;
    }


    public void setMediaDocumentsInIndex(int mediaDocumentsInIndex) {
        this.mediaDocumentsInIndex = mediaDocumentsInIndex;
    }


    /**
     *
     * How many webcrawl documents there was in index
     *
     * @return
     */
    public int getWebCrawlDocumentsInIndex() {
        return webCrawlDocumentsInIndex;
    }


    public void setWebCrawlDocumentsInIndex(int webCrawlDocumentsInIndex) {
        this.webCrawlDocumentsInIndex = webCrawlDocumentsInIndex;
    }


    /**
     *
     * How many wiki documents there was in index
     *
     * @return
     */
    public int getWikiDocumentsInIndex() {
        return wikiDocumentsInIndex;
    }


    public void setWikiDocumentsInIndex(int wikiDocumentsInIndex) {
        this.wikiDocumentsInIndex = wikiDocumentsInIndex;
    }

    /**
     * Returns all search <code>SearchResultElement</code> results available from the search.
     * @return
     */
    public Collection getAllResults() {
        List allResults = (ArrayList)getResults();
        allResults.addAll(getRetrieverResults());
        allResults.addAll(getWikiResult());
        return allResults;
    }


    public void setCompaniesDocumentsInIndex(int companiesDocumentsInIndex) {
        this.companiesDocumentsInIndex = companiesDocumentsInIndex;
    }

    public void addPersonsResult(SearchResultElement result) {
        personsResults.add(result);
    }

    public List getPersonsResults() {
        return personsResults;
    }

    public void addCategoryModifier(IModifier modifier) {
        categoryModifiers.add(modifier);
    }

    public List getCategoryModifiers() {
        return categoryModifiers;
    }

    public void setCategoryModifiers(List categoryModifiers) {
        this.categoryModifiers = categoryModifiers;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }


    public void addMoreoverResult(SearchResultElement result) {
        moreoverResults.add(result);
    }

    public List getMoreoverResults() {
        return moreoverResults;
    }

    public void setMoreoverResults(List moreoverResults) {
        this.moreoverResults = moreoverResults;
    }

    public void addNordicNewsResult(SearchResultElement result) {
        nordicNewsResults.add(result);
    }

    public List getNordicNewsResults() {
        return nordicNewsResults;
    }
}
