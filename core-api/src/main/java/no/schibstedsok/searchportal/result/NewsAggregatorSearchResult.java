package no.schibstedsok.searchportal.result;

import no.schibstedsok.searchportal.mode.command.SearchCommand;

import java.util.Map;
import java.util.HashMap;

public class NewsAggregatorSearchResult extends BasicSearchResult {
    private Map<String, SearchResult> relatedResults = new HashMap<String, SearchResult>();

    public NewsAggregatorSearchResult(SearchCommand command) {
        super(command);
    }

    public void addRelatedResultItem(String name, SearchResultItem searchResultItem) {
        SearchResult searchResult = relatedResults.get(name);
        if (searchResult == null) {
            searchResult = new BasicSearchResult(getSearchCommand());
            relatedResults.put(name, searchResult);
        }
        searchResult.addResult(searchResultItem);
    }

    public SearchResult getRelatedResult(String name) {
        return relatedResults.get(name);
    }

    public String toString() {
        return "NewsAggregatorSearchResult{" +
                "super=" + super.toString() +
                ", relatedResults=" + relatedResults +
                '}';
    }
}
