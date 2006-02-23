// Copyright (2006) Schibsted Søk AS
/*
 * OlympicSearchCommand.java
 *
 * Created on February 8, 2006, 2:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.command.impl.SearchCommandFactory;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.OlympicSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.PicSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfigurationContext;
import no.schibstedsok.front.searchportal.query.transform.AbstractQueryTransformer;
import no.schibstedsok.front.searchportal.query.transform.ExactTitleMatchTransformer;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import no.schibstedsok.front.searchportal.util.OlympicData;

public class OlympicSearchCommand extends AbstractSearchCommand {

    private OlympicSearchConfiguration configuration;


    public OlympicSearchCommand(final SearchCommand.Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    public SearchResult execute() {
        final OlympicData data = OlympicData.instance();

        final SearchResult result = new BasicSearchResult(this);

        result.setHitCount(0);

        if (data.getParticipants().containsKey(context.getRunningQuery().getQueryString().toLowerCase(getRunningQuery().getLocale()))) {
            final SearchResultItem resultItem = getParticipantItem(data, result);
            result.setHitCount(1);
            final SearchResult picSearchResult = doPicSearch(null);


            if (picSearchResult != null && picSearchResult.getHitCount() > 0) {
                final SearchResultItem picture = (SearchResultItem) picSearchResult.getResults().get(0);
                resultItem.addField("pictureHits", new Integer(picSearchResult.getHitCount()).toString());
                resultItem.addField("thumb_url", picture.getField("thumb_url"));
                resultItem.addField("page_url", picture.getField("page_url"));
            }

            final SearchResult wikiResult = doWikiSearch();


            if (wikiResult != null && wikiResult.getHitCount() > 0) {
                final SearchResultItem wikiItem = (SearchResultItem) wikiResult.getResults().get(0);
                resultItem.addField("wikiUrl", wikiItem.getField("url"));
            }

            result.addResult(resultItem);
        } else if (data.getDiciplines().containsKey(context.getRunningQuery().getQueryString().toLowerCase(getRunningQuery().getLocale()))) {
            result.setHitCount(1);
            final SearchResultItem resultItem = getDiciplineItem(data, result);

            final SearchResult picSearchResult = doPicSearch(resultItem.getField("alternativePicSearch"));

            resultItem.addNestedSearchResult("picSearch", picSearchResult);

            final SearchResult wikiResult = doWikiSearch();

            if (wikiResult != null && wikiResult.getHitCount() > 0) {
                final SearchResultItem wikiItem = (SearchResultItem) wikiResult.getResults().get(0);
                resultItem.addField("wikiUrl", wikiItem.getField("url"));
            }

            final List participants = (List) data.getParticipantsPerDicipline().get(context.getRunningQuery().getQueryString().toLowerCase(getRunningQuery().getLocale()));

            if (participants != null) {
                for (Iterator it = participants.iterator(); it.hasNext();) {
                    final String elem = (String) it.next();
                    resultItem.addToMultivaluedField("participants", elem);
                }
            }
            result.addResult(resultItem);
        } else {
            result.setHitCount(1);
            final SearchResultItem item = new BasicSearchResultItem();
            result.addResult(item);
        }

        return result;
    }

    private SearchResult doWikiSearch() {


        final FastConfiguration wikiConf = new FastConfiguration();
        wikiConf.setQueryServerURL(((OlympicSearchConfiguration) context.getSearchConfiguration()).getWikiQrServer());
        wikiConf.setSortBy("standard");
        wikiConf.addCollection("wikipedia2");
        wikiConf.setResultsToReturn(1);
        wikiConf.addQueryTransformer(new ExactTitleMatchTransformer());
        wikiConf.addResultField("url");
        final SearchCommand wikiCommand = SearchCommandFactory.createSearchCommand(getCommandContext(wikiConf), getParameters());
        SearchResult wikiResult = null;
        try {
            wikiResult = (SearchResult) wikiCommand.call();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return wikiResult;
    }

    private SearchResult doPicSearch(final String alternative) {

        final PicSearchConfiguration pc = new PicSearchConfiguration();

        pc.addResultField("thumb_url");
        pc.addResultField("page_url");
        pc.addResultField("thumb_width");
        pc.addResultField("thumb_height");
        pc.addResultField("height");
        pc.addResultField("width");
        pc.addResultField("filesize");
        pc.setResultsToReturn(3);

        if (alternative != null) {
            pc.addQueryTransformer(new AbstractQueryTransformer() {
                public String getTransformedQuery(final String query) {
                    return alternative;
                }
            });
        }

        final SearchCommand picSearch = SearchCommandFactory.createSearchCommand(getCommandContext(pc), getParameters());
        SearchResult picSearchResult = null;
        try {
            picSearchResult = (SearchResult) picSearch.call();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return picSearchResult;
    }

    private SearchCommand.Context getCommandContext(final SearchConfiguration c) {

        final SearchCommand.Context cxt = (SearchCommand.Context) ContextWrapper.wrap(
                SearchCommand.Context.class,
                new BaseContext[]{
                    context,
                    new SearchConfigurationContext() {
                        public SearchConfiguration getSearchConfiguration() {
                            return c;
                        }
                    }
        });
        return cxt;
    }

    private SearchResultItem getParticipantItem(final OlympicData data, final SearchResult result) {
        final Map participant = (Map) data.getParticipants().get(context.getRunningQuery().getQueryString().toLowerCase(context.getRunningQuery().getLocale()));

        final SearchResultItem resultItem = new BasicSearchResultItem();
        resultItem.addField("participantName", context.getRunningQuery().getQueryString());
        resultItem.addField("diciplineName", (String) participant.get("diciplineName"));

        if (participant.containsKey("infoLink1")) {
            resultItem.addField("infoLink1", (String) participant.get("infoLink1"));
        }
        if (participant.containsKey("infoLink2")) {
            resultItem.addField("infoLink2", (String) participant.get("infoLink2"));
        }
        if (participant.containsKey("infoLink3")) {
            resultItem.addField("infoLink3", (String) participant.get("infoLink3"));
        }

        final Map dicipline =  (Map) data.getDiciplines().get((String) participant.get("diciplineName"));

        if (dicipline != null) {
            if (dicipline.containsKey("tvLink")) {
                resultItem.addField("tvLink", (String) dicipline.get("tvLink"));
            }

            if (dicipline.containsKey("programLink")) {
                resultItem.addField("programLink", (String) dicipline.get("programLink"));
            }
        }
        return resultItem;
    }

    private SearchResultItem getDiciplineItem(final OlympicData data, final SearchResult result) {
        final Map dicipline = (Map) data.getDiciplines().get(context.getRunningQuery().getQueryString().toLowerCase(context.getRunningQuery().getLocale()));

        final SearchResultItem resultItem = new BasicSearchResultItem();

        resultItem.addField("diciplineName", context.getRunningQuery().getQueryString());
        resultItem.addField("tvLink", (String) dicipline.get("tvLink"));
        resultItem.addField("programLink", (String) dicipline.get("programLink"));
        resultItem.addField("alternativePicSearch", (String) dicipline.get("alternativePicSearch"));
        return resultItem;
    }
}
