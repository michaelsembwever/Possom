/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
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
