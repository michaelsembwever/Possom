/*
 * RunningQueryTransformer.java
 *
 * Created on 13/06/2007, 12:15:21
 *
 */

package no.sesat.search.run.handler;

import no.sesat.search.datamodel.DataModelContext;
import no.sesat.search.site.config.PropertiesContext;
import no.sesat.search.site.config.BytecodeContext;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.site.SiteContext;

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
