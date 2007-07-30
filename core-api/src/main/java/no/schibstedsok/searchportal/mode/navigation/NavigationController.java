/* Copyright (2005-2007) Schibsted Søk AS
*
* Jul 20, 2007 11:20:16 AM
*/
package no.schibstedsok.searchportal.mode.navigation;

import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.DataModelContext;
import no.schibstedsok.searchportal.site.config.ResourceContext;
import no.schibstedsok.searchportal.site.SiteContext;

/**
 * TODO: Move into sesat-search-command-control-spi once that module is ready for action.
 */
public interface NavigationController {

    interface Context extends DataModelContext, ResourceContext, SiteContext {
    }

    NavigationItem getNavigationItems(Context context);
}
