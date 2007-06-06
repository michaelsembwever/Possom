package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.result.FastSearchResult;
import org.apache.log4j.Logger;

/**
 * Adapts a old searchresult to use the newsSimpeOffsetPager
 *
 * @author Geir H. Pettersn (T-Rank)
 */
public class ClusterOffsetAdapter implements ResultHandler {
    private static final Logger LOG = Logger.getLogger(ClusterOffsetAdapter.class);
    private ClusterOffsetAdapterResultHandlerConfig config;

    public ClusterOffsetAdapter(final ResultHandlerConfig config) {
        this.config = (ClusterOffsetAdapterResultHandlerConfig) config;
    }

    public void handleResult(Context cxt, DataModel datamodel) {
        if (cxt.getSearchResult() instanceof FastSearchResult) {
            int offsetInt = 0;
            FastSearchResult searchResult = (FastSearchResult) cxt.getSearchResult();
            StringDataObject offset = datamodel.getParameters().getValue(config.getOffsetField());
            if (offset != null) {
                try {
                    offsetInt = Integer.parseInt(offset.getString());
                } catch (NumberFormatException e) {
                    LOG.error("Could not parse offset", e);
                }
            }
            offsetInt += config.getOffsetInterval();
            if (offsetInt < searchResult.getHitCount()) {
                searchResult.addField(config.getOffsetResultField(), Integer.toString(offsetInt));
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.error("Can only adapt FastSearchResults");
            }
        }
    }
}
