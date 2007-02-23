package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.config.TvEnrichSearchConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 * A search command that combine the results from TV and WebTV search.
 */
public class TvEnrichSearchCommand extends StaticSearchCommand {

    private static final Logger LOG = Logger.getLogger(TvEnrichSearchCommand.class);

    public TvEnrichSearchCommand(
            final Context cxt,
            final DataModel datamodel) {

        super(cxt, datamodel);
    }

    public SearchResult execute() {
        int hitCount = 0;

        final SearchResult result = new BasicSearchResult(this);
        TvEnrichSearchConfiguration tesc = (TvEnrichSearchConfiguration) this.getSearchConfiguration();
        if (tesc.getWaitOn() != null) {
            final String waitOn = tesc.getWaitOn();

            final String[] cmds = waitOn.split(",");
            try {
                for (String cmd : cmds) {
                    final FastSearchResult fsr = (FastSearchResult) context.getRunningQuery().getSearchResult(cmd);
                    hitCount += fsr.getHitCount();
                    result.getResults().addAll(fsr.getResults());
                }
            } catch (Exception e) {
                LOG.error(e);
                return new BasicSearchResult(this);
            }
        }

        result.setHitCount(hitCount);
        return result;
    }
}
