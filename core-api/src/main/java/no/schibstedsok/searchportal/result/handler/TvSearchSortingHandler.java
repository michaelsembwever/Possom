// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.config.TvsearchCommandConfig;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * TvSearchSortingHandler sorts the result by channel, day or category
 * @author ajamtli
 * @version $Id$
 */
public final class TvSearchSortingHandler implements ResultHandler {
    
    private final TvsearchSortingResultHandlerConfig config;
    
    public TvSearchSortingHandler(final ResultHandlerConfig config){
        this.config = (TvsearchSortingResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        final Map<String,Object> parameters = datamodel.getJunkYard().getValues();
        final String sortBy = parameters.containsKey("userSortBy") ? ((String)parameters.get("userSortBy")) : "channel";

        final TvsearchCommandConfig searchConfiguration = (TvsearchCommandConfig) cxt.getSearchConfiguration();

        HashMap<String,ArrayList<ResultItem>> hm = new HashMap<String,ArrayList<ResultItem>>();
        ResultList<ResultItem> sr = cxt.getSearchResult();
        final int resultsPerBlock = config.getResultsPerBlock();
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

        if (parameters.get("nav_channels") != null
                && parameters.get("nav_days") != null && !sortBy.equals("category")) {
            return;
        }

        if (parameters.get("nav_days") != null && sortBy.equals("day")) {
            return;
        }

        /* Split search result */
        for (ResultItem sri : sr.getResults()) {
            String fieldValue = sri.getField(field);
            if (!hm.containsKey(fieldValue)) {
                hm.put(fieldValue, new ArrayList<ResultItem>());
            }

            List<ResultItem> results = hm.get(fieldValue);

            if (results.size() < resultsPerBlock) {
                results.add(sri);
            }
        }

        sr.removeResults();


        if (sortBy.equals("channel")) {
            if (datamodel.getQuery().getQuery().isBlank()) {
                for (String channel : searchConfiguration.getDefaultChannels()) {
                    if (hm.containsKey(channel)) {
                        sr.addResults(hm.get(channel));
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
                    sr.addResults(hm.get(weekDay));
                }
            }
        } else if (sortBy.equals("category")) {
            joinBlocks(cxt, hm, "categories");
        }
    }

    private final ResultList<ResultItem> joinBlocks(final Context cxt, final HashMap hm, final String modifiersId) {

        final List<Modifier> modifiers = ((FastSearchResult) cxt.getSearchResult()).getModifiers(modifiersId);
        final ResultList<ResultItem> sr = cxt.getSearchResult();

        if (modifiers == null) {
            return sr;
        }

        int i = 0;
        for (Modifier modifier : modifiers) {
            if (i == config.getBlocksPerPage()) {
                break;
            }
            if (hm.containsKey(modifier.getName())) {
                sr.addResults((List<ResultItem>)hm.get(modifier.getName()));
            }
            i++;
        }
        return sr;
    }
}
