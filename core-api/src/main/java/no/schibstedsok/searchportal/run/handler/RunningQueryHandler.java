/*
 * RunningQueryTransformer.java
 *
 * Created on 13/06/2007, 12:15:21
 *
 */

package no.schibstedsok.searchportal.run.handler;

import no.schibstedsok.searchportal.datamodel.DataModelContext;
import no.schibstedsok.searchportal.site.config.PropertiesContext;
import no.schibstedsok.searchportal.site.config.BytecodeContext;
import no.schibstedsok.searchportal.site.SiteContext;

/**
 *
 * @author mick
 * @version $Id$
 */
public interface RunningQueryHandler {

    interface Context extends DataModelContext, PropertiesContext, SiteContext, BytecodeContext {
    }

    void handleRunningQuery(final Context context);
}
