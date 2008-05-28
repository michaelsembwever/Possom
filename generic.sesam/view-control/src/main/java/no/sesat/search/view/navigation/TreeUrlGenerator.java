/**
 * Copyright (2008) Schibsted Søk AS
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
 *
 */

package no.sesat.search.view.navigation;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * This class is a url generator for the TreeNavigationController class.
 */
public class TreeUrlGenerator extends AbstractUrlGenerator {

    public TreeUrlGenerator(DataModel dataModel, NavigationConfig.Navigation navigation, NavigationState state) {
        super(dataModel, navigation, state);

    }

    public String getURL(String value, NavigationConfig.Nav nav){
        return getURL(value, nav, new HashMap<String, String>());
    }

    /**
     * Generate url for the given TreeNavigationConfiguration.
     *
     * @param value
     * @param nav
     * @param extraParameters
     * @return The url
     */
    public String getURL(String value, NavigationConfig.Nav nav, Map<String, String> extraParameters) {
        String url = getPrefix() + "?";

        Set <String> parameters = getUrlComponentNames(nav, extraParameters.keySet(), value);

        parameters.addAll(getNavigationState().getParameterNames(nav, false));

        if (nav instanceof TreeNavigationConfig) {
            TreeNavigationConfig n = (TreeNavigationConfig)nav;
            parameters.addAll(Arrays.asList(n.getParametersToKeep().split(" *, *")));

            Set<String> remove = ((TreeNavigationConfig)nav).getResetParameter();

            parameters.removeAll(remove);

            for(String param: parameters) {
                url += generateUrlParameter(param, getUrlComponentValue(nav, param, extraParameters));
            }

            while(n != null) {
                if(!n.isHideParameter()) {
                    url += generateUrlParameter(n.getField(), n.getValue());
                }
                n = (TreeNavigationConfig)n.getParent();
            }
        }

        return url;
    }

    /**
     * Generate a string containing the name and the value as a correct encoded url part.
     *
     * @param name
     * @param value
     * @return
     */
    public static String generateUrlParameter(final String name, final String value) {
        if (null != value && value.length() > 0) {
            return enc(name) + "=" + value + "&amp;";
        }
        return "";
    }
}
