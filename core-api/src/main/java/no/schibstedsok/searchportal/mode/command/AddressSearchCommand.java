/*
 * Copyright (2005-2007) Schibsted Søk AS
 */


package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * @author <a href="mailto:anders@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Revision$</tt>
 */
public class AddressSearchCommand extends AbstractSimpleFastSearchCommand{

    public AddressSearchCommand(final Context cxt) {
        super(cxt);
    }

    public final SearchResult execute() {
        SearchResult sr = super.execute();

        if (getSearchConfiguration().isCollapsing()) {
            String prevCollapseId = "";
            for (SearchResultItem item : sr.getResults()) {
                if (item.getField("collapseid").equals(prevCollapseId)) {
                    sr.getResults().remove(item);
                }
            }
        }
        
        return sr;
    }
}
