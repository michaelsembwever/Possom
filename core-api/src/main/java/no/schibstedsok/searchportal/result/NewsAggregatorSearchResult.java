// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import no.schibstedsok.searchportal.mode.command.SearchCommand;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class NewsAggregatorSearchResult extends BasicSearchResult {
    private Map<String, SearchResult> relatedResults = new HashMap<String, SearchResult>();
    private Map<String, List<Navigation>> nav = new HashMap<String, List<Navigation>>();

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

    public List<Navigation> getNavigationList(String navigationType) {
        return nav.get(navigationType);
    }

    public void addNavigation(String navigationType, Navigation navigation) {
        List<Navigation> navList = nav.get(navigationType);
        if (navList == null) {
            navList = new ArrayList<Navigation>();
            nav.put(navigationType, navList);
        }
        navList.add(navigation);
    }

    public String toString() {
        return "NewsAggregatorSearchResult{" +
                "super=" + super.toString() +
                ", relatedResults=" + relatedResults +
                ", nav=" + nav +
                '}';
    }

    public static class Navigation {
        private String type;
        private String name;
        private String xmlFile;


        public Navigation(String type, String name, String xmlFile) {
            this.type = type;
            this.name = name;
            this.xmlFile = xmlFile;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getXmlFile() {
            return xmlFile;
        }

        public void setXmlFile(String xmlFile) {
            this.xmlFile = xmlFile;
        }
    }

}
