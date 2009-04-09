/* Copyright (2006-2007) Schibsted ASA
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
package no.sesat.search.query.transform;

import java.util.Map;

import no.sesat.search.datamodel.generic.StringDataObject;

/**
 * Transformes the query if the requestparameters contains a contentId.
 *
 *
 * @version $Revision:$
 */
public class MapInfoPageQueryTransformer extends AbstractQueryTransformer {

    private final MapInfoPageQueryTransformerConfig config;

    /** Required constructor.
     * @param config Query transformer config
     */
    public MapInfoPageQueryTransformer(final QueryTransformerConfig config){
        this.config = (MapInfoPageQueryTransformerConfig) config;
    }

    /**
     * If the request parameteters contains the contentid parameter, append recordid to the query.
     *
     * @see no.sesat.search.query.transform.QueryTransformer
     */
    public String getTransformedQuery() {
        final String originalQuery = getContext().getTransformedQuery();
        Map<String,StringDataObject> requestParameters = getContext().getDataModel().getParameters().getValues();

        if(requestParameters != null && requestParameters.containsKey(config.getParameterName())){
            return config.getPrefix() + ":" + requestParameters.get(config.getParameterName()).getString();
        }

        return originalQuery;
    }

    public String getFilter() {
        Map<String,StringDataObject> requestParameters = getContext().getDataModel().getParameters().getValues();

        if(requestParameters != null && requestParameters.containsKey(config.getParameterName()) &&
                requestParameters.containsKey(config.getFilterParameterName())){
            return "+" + config.getFilterPrefix() + ":'" + requestParameters.get(config.getFilterParameterName()).getString() + "'";
        }

        return "";
    }
}
