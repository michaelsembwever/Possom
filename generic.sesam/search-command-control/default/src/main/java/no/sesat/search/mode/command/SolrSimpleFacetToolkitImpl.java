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
import no.sesat.search.result.FacetedSearchResult;
import no.sesat.search.result.Modifier;
import no.sesat.search.result.Navigator;
import no.sesat.search.result.ResultItem;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Solr's Simple Faceting toolkit.
 *
 * {@link http://wiki.apache.org/solr/SolrFacetingOverview}
 * {@link http://wiki.apache.org/solr/SimpleFacetParameters}
 */
public class SolrSimpleFacetToolkitImpl implements SolrSearchCommand.FacetToolkit {

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    public void createFacets(final SearchCommand.Context context, final SolrQuery query) {

        final Map<String, Navigator> facets = getSearchConfiguration(context).getFacets();
        query.setFacet(0 < facets.size());

        // facet counters || selection
        for (final Navigator facet : facets.values()) {
            createFacet(context, facet, query);
        }
    }

    public void collectFacets(
            final SearchCommand.Context context,
            final QueryResponse response,
            final FacetedSearchResult<? extends ResultItem> searchResult) {

        final Map<String, Navigator> facets = getSearchConfiguration(context).getFacets();
        for (final Navigator facet : facets.values()) {
            final FacetField field = response.getFacetField(facet.getId());
            // facet counters
            if(null != field && null != field.getValues()){
                for (FacetField.Count c : field.getValues()){
                    final Modifier mod = new Modifier(c.getName(), (int)c.getCount(), facet);
                    searchResult.addModifier(facet.getId(), mod);
                }
            }
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void createFacet(final SearchCommand.Context context, final Navigator facet, final SolrQuery query){

        // we want the facet count
        query.addFacetField(facet.getField());

        final StringDataObject facetValue = context.getDataModel().getParameters().getValue(facet.getId());

        if (null != facetValue) {
            // splitting here allows for multiple navigation selections within the one navigation level.
            for (String navSingleValue : facetValue.getString().split(",")) {

                final String value = facet.isBoundaryMatch()
                        ? "^\"" + navSingleValue + "\"$"
                        : "\"" + navSingleValue + "\"";

                query.addFilterQuery(facet.getField() + ':' + value);
            }
        }
    }

    private FacetedCommandConfig getSearchConfiguration(final SearchCommand.Context context) {
        return (FacetedCommandConfig) context.getSearchConfiguration();
    }

    // Inner classes -------------------------------------------------

}
