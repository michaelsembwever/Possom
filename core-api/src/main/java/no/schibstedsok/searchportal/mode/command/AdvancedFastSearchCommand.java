// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.SearchType;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.DataModel;

/**
 *
 */
public class AdvancedFastSearchCommand extends AbstractAdvancedFastSearchCommand {

    /** Creates a new instance of an AdvancedFastSearchCommand.
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public AdvancedFastSearchCommand(final Context cxt) {

        super(cxt);
    }

    // TODO comment me.
    /** TODO comment me. **/
    protected void setAdditionalParameters(final ISearchParameters params) {
        super.setAdditionalParameters(params);
        params.setParameter(new SearchParameter(BaseParameter.TYPE, SearchType.SEARCH_ADVANCED.getValueString()));
    }
}
