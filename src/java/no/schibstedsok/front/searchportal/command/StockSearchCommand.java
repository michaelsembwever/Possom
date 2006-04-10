/*
 * StockSearchCommand.java
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Map;
import no.schibstedsok.front.searchportal.query.transform.SynonymQueryTransformer;

import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;

/**
 *
 * @author magnuse
 */
public final class StockSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(StockSearchCommand.class);

    public StockSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    public SearchResult execute() {

        final SearchResult result = new BasicSearchResult(this);
        final String q = getTransformedQuery();
        LOG.info("transformed query is " + q);

        // for now we are only interested in complete matches. and the SynonymQT only deals with stock-tickers.
        if( SynonymQueryTransformer.isSynonym( q )){

            final SearchResultItem item = new BasicSearchResultItem();
            

            final String tickerCode = SynonymQueryTransformer.isTicker(q)
                    ? q
                    : SynonymQueryTransformer.getSynonym(q);
           final String tickerName = SynonymQueryTransformer.isTickersFullname(q)
                    ? q
                    : SynonymQueryTransformer.getSynonym(q);

            item.addField("tickerCode", tickerCode);
            item.addField("tickerName", tickerName);

            result.addResult(item);
            result.setHitCount(1);
        }
        return result;
    }

}
