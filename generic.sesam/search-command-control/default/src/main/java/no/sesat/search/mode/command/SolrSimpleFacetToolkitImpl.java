/*
 * Copyright (2009) Schibsted Søk AS
 * This file is part of SESAT.
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
 */
package no.sesat.search.mode.command;

import java.util.Map;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.mode.config.FacetedCommandConfig;
import no.sesat.search.result.Navigator;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * Solr's Simple Faceting toolkit.
 *
 * {@link http://wiki.apache.org/solr/SolrFacetingOverview}
 * {@link http://wiki.apache.org/solr/SimpleFacetParameters}
 */
public class SolrSimpleFacetToolkitImpl implements SolrSearchCommand.FacetToolkit {

    public void createFacets(final SearchCommand.Context context, final SolrQuery query) {

        final Map<String, Navigator> facets = getSearchConfiguration(context).getFacets();
        query.setFacet(0 < facets.size());

        // facet counters
        for (Navigator facet : facets.values()) {
            query.addFacetField(facet.getField());
        }

        // facet selection
        for (final Navigator facet : facets.values()) {
            final StringDataObject facetValue = context.getDataModel().getParameters().getValue(facet.getId());

            if (null != facetValue) {
                // splitting here allows for multiple navigation selections within the one navigation level.
                for (String navSingleValue : facetValue.getString().split(",")) {

                    final String value = facet.isBoundaryMatch()
                            ? "^\"" + navSingleValue + "\"$"
                            : "\"" + navSingleValue + "\"";

                    query.addFacetQuery(facet.getField() + ':' + value);
                }
            }
        }
    }

    private FacetedCommandConfig getSearchConfiguration(final SearchCommand.Context context) {
        return (FacetedCommandConfig) context.getSearchConfiguration();
    }
}
