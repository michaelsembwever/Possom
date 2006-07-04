// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    
    /** Number of results per block of channels, days or categories. **/
    private int resultsPerBlock;
    
    /** Number of blocks to display per page. **/
    private int blocksPerPage;
     
    public int getResultsPerBlock() {
        return resultsPerBlock;
    }
    
    public void setResultsPerBlock(int resultsPerBlock) {
        this.resultsPerBlock = resultsPerBlock;
    }
    
    public int getBlocksPerPage() {
        return blocksPerPage;
    }
    
    public void setBlocksPerPage(int blocksPerPage) {
        this.blocksPerPage = blocksPerPage;
    }
    
    public void handleResult(final Context cxt, final Map parameters) {
        final String sortBy = parameters.containsKey("userSortBy") ? ((String)parameters.get("userSortBy")) : "channel";
        
        final TvSearchConfiguration searchConfiguration = (TvSearchConfiguration) cxt.getSearchResult().getSearchCommand().getSearchConfiguration();
        
        HashMap<String,ArrayList<SearchResultItem>> hm = new HashMap();
        SearchResult sr = cxt.getSearchResult();
        final int resultsPerBlock = getResultsPerBlock();
        String field = "channel";
        
        if (sortBy.equals("day")) {
            field = "weekday";
        } else if (sortBy.equals("category")) {
            field = "category";
        }

        /* Return all results if user has chosen to view one channel */
        if (parameters.get("nav_channels") != null && sortBy.equals("channel")) {
            return;
        }
        
        if (parameters.get("nav_categories") != null && sortBy.equals("category")) {
            return;
        }
        
        if (parameters.get("nav_channels") != null && parameters.get("nav_days") != null) {
            return;
        }
        
        if (parameters.get("nav_days") != null && sortBy.equals("day")) {
            return;
        }
        
        /* Split search result */
        for (SearchResultItem sri : sr.getResults()) {
            String fieldValue = sri.getField(field);
            if (!hm.containsKey(fieldValue)) {
                hm.put(fieldValue, new ArrayList<SearchResultItem>());
            }
            
            List<SearchResultItem> results = hm.get(fieldValue);
            
            if (results.size() < resultsPerBlock) {
                results.add(sri);
            }
        }
        
        sr.getResults().clear();
        
        
        if (sortBy.equals("channel")) {
            if (cxt.getQuery().isBlank()) {
                for (String channel : searchConfiguration.getDefaultChannels()) {
                    if (hm.containsKey(channel)) {
                        sr.getResults().addAll(hm.get(channel));
                    }
                }
                /* Check if the search gave results for the default channels */
                if (sr.getResults().size() == 0) {
                    joinBlocks(cxt, hm, "channels");
                }
            } else {
                joinBlocks(cxt, hm, "channels");
            }
        } else if (sortBy.equals("day")) {
            Calendar cal = Calendar.getInstance();
            int startDay = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            
            for (int i = 0; i < 7; i++) {
                String weekDay = Integer.toString((startDay + i) % 7);
                if (hm.containsKey(weekDay)) {
                    sr.getResults().addAll(hm.get(weekDay));
                }
            }
        } else if (sortBy.equals("category")) {
            joinBlocks(cxt, hm, "categories");
        }
    }
    
    private final SearchResult joinBlocks(final Context cxt, final HashMap hm, final String modifiersId) {
        final TvSearchConfiguration searchConfiguration = (TvSearchConfiguration) cxt.getSearchResult().getSearchCommand().getSearchConfiguration();
        final List<Modifier> modifiers = ((FastSearchResult) cxt.getSearchResult()).getModifiers(modifiersId);
        final SearchResult sr = cxt.getSearchResult();
        
        if (modifiers == null) {
            return sr;
        }
        
        int i = 0;
        for (Modifier modifier : modifiers) {
            if (i == getBlocksPerPage()) {
                break;
            }
            if (hm.containsKey(modifier.getName())) {
                sr.getResults().addAll((List<SearchResultItem>)hm.get(modifier.getName()));
            }
            i++;
        }
        return sr;
    }
}
