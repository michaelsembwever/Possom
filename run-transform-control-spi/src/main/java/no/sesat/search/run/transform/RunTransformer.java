/*
 * Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */

package no.sesat.search.run.transform;

import java.io.Serializable;
import java.util.Collection;
import no.sesat.search.datamodel.DataModelContext;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.PropertiesContext;
import no.sesat.search.site.config.ResourceContext;

/**
 *
 *
 */
public interface RunTransformer extends Serializable {
    interface Context extends DataModelContext, PropertiesContext, SiteContext, ResourceContext {
        Collection<SearchConfiguration> getApplicableSearchConfigurations();
    }

    Collection<SearchConfiguration> transform(final Context context);
}
