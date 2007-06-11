// Copyright (2007) Schibsted Søk AS
/*
 * StockSearchCommand.java
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.query.transform.SynonymQueryTransformer;
import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.result.BasicResultItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.log4j.Logger;

/**
 *
 * @author magnuse
 * @version $Id$
 */
public final class StockSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(StockSearchCommand.class);

    /**
     * 
     * @param cxt 
     */
    public StockSearchCommand(final Context cxt) {

        super(cxt);
    }

    public ResultList<? extends ResultItem> execute() {

        final ResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final String q = getTransformedQuery();
        LOG.info("transformed query is " + q);

        // TODO: Remove this dependency on the query transformer. Prevents the query transformer from being moved into
        // the skin.
        // for now we are only interested in complete matches. and the SynonymQT only deals with stock-tickers.
        if( SynonymQueryTransformer.isSynonym( q )){

            ResultItem item = new BasicResultItem();


            final String tickerCode = SynonymQueryTransformer.isTicker(q)
                    ? q
                    : SynonymQueryTransformer.getSynonym(q);
            final String tickerName = SynonymQueryTransformer.isTickersFullname(q)
                    ? q
                    : SynonymQueryTransformer.getSynonym(q);

            
            item = item.addField("tickerCode", tickerCode).addField("tickerName", tickerName);

            result.addResult(item);
            result.setHitCount(1);
        }
        return result;
    }

}
