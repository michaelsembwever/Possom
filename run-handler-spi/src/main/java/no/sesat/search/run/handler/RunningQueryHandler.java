/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
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
