// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.schibstedsok.front.searchportal.configuration.TvSearchConfiguration;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

/**
 * TvSearchSortingHandler sorts the result by channel, day or category
 * @author ajamtli
 * @version $Id$
 */
public class TvSearchSortingHandler implements ResultHandler {
    
    public void handleResult(final Context cxt, final Map parameters) {
        final String sortBy = parameters.containsKey("userSortBy") ? ((String [])parameters.get("userSortBy"))[0] : "channel";
        
        final TvSearchConfiguration searchConfiguration = (TvSearchConfiguration) cxt.getSearchResult().getSearchCommand().getSearchConfiguration();
        
        if (sortBy.equals("channel")) {
            HashMap<String,ArrayList<SearchResultItem>> hm = new HashMap();
            SearchResult sr = cxt.getSearchResult();
            final int resultsPerChannel = searchConfiguration.getResultsPerChannel();
            
            /* Split search result in channels */
            for (SearchResultItem sri : sr.getResults()) {
                String channel = sri.getField("channel");
                if (!hm.containsKey(channel)) {
                    hm.put(channel, new ArrayList<SearchResultItem>());
                }

                List<SearchResultItem> channelResults = hm.get(channel);

                if (channelResults.size() < resultsPerChannel) {
                    channelResults.add(sri);
                }
            }

            sr.getResults().clear();
            if (cxt.getQuery().isBlank()) {
                for (String channel : searchConfiguration.getDefaultChannels()) {
                    if (hm.containsKey(channel)) {
                        sr.getResults().addAll(hm.get(channel));
                    }
                }
            } else {
                final List<Modifier> modifiers = ((FastSearchResult) cxt.getSearchResult()).getModifiers("channels");
                int i = 0;
                for (Modifier modifier : modifiers) {
                    if (i > searchConfiguration.getChannelsPerPage()) {
                        break;
                    }
                    if (hm.containsKey(modifier.getName())) {
                        sr.getResults().addAll(hm.get(modifier.getName()));
                    }
                    i++;
                }
            }
        } else if (sortBy.equals("day")) {
            HashMap<String,ArrayList<SearchResultItem>> hm = new HashMap();
            SearchResult sr = cxt.getSearchResult();
            final int resultsPerDay = searchConfiguration.getResultsPerChannel();
            
            /* Split results into days */
            for (SearchResultItem sri : sr.getResults()) {
                String weekday = sri.getField("weekday");
                if (!hm.containsKey(weekday)) {
                    hm.put(weekday, new ArrayList<SearchResultItem>());
                }
                
                List<SearchResultItem> weekdayResults = hm.get(weekday);
                
                if (weekdayResults.size() < resultsPerDay) {
                    weekdayResults.add(sri);
                }
            }
            
            sr.getResults().clear();
            
            for (int i = 0; i < 7; i++) {
                String weekDay = Integer.toString(i);
                if (hm.containsKey(weekDay)) {
                    sr.getResults().addAll(hm.get(weekDay));
                }
            }
        } else if (sortBy.equals("category")) {
            
        }
    }
}
