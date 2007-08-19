/*
 * RunningQueryTransformer.java
 *
 * Created on 13/06/2007, 12:15:21
 *
 */

package no.sesat.searchportal.run.handler;

import no.sesat.searchportal.datamodel.DataModelContext;
import no.sesat.searchportal.site.config.PropertiesContext;
import no.sesat.searchportal.site.config.BytecodeContext;
import no.sesat.searchportal.site.config.ResourceContext;
import no.sesat.searchportal.site.SiteContext;

/**
 *
 * @author mick
 * @version $Id$
 */
public interface RunningQueryHandler {
                               
    interface Context extends DataModelContext, PropertiesContext, SiteContext, ResourceContext {
    }

    void handleRunningQuery(final Context context);
}
