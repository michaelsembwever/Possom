/*
 * Copyright (2007-2008) Schibsted Søk
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
 */

package no.sesat.search.view.navigation;

import no.sesat.search.datamodel.DataModel;

import java.util.Map;
import java.util.Set;

/**
 * The basics of a UrlGenerator.
 *
 * Extend this class to create your own UrlGenerator. This ensures that configuration contracts are upheld.
 *
 * The term url component is used to denote a part of a URL, be it a part of the path or a parameter.
 *
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 * @version $Id$
 */
public abstract class AbstractUrlGenerator implements UrlGenerator {

    private final DataModel dataModel;
    private final NavigationState state;
    private final NavigationConfig.Navigation navigation;

    /**
     * Overriding classes must have just one constructor with a signature matching this constructor.
     *
     * @param dataModel The datamodel.
     * @param navigation the navigator set to generate URLs for.
     * @param state the current navigation set.
     */
    public AbstractUrlGenerator(
            final DataModel dataModel,
            final NavigationConfig.Navigation navigation,
            final NavigationState state) {

        this.navigation = navigation;
        this.dataModel = dataModel;
        this.state = state;
    }

    /**
     * Returns the navigation state.
     *
     * @return the navigation state.
     */
    protected final NavigationState getNavigationState() {
        return state;
    }

    /**
     * Returns the static URL prefix. (e.g. /search). If the URL is configured to be relative, the empty string is
     * returned.
     *
     * @see no.sesat.search.view.navigation.NavigationConfig.Navigation#getPrefix()
     *
     * @return the url prefix.
     */
    protected String getPrefix() {
        return "RELATIVE".equals(navigation.getPrefix()) ? "" : navigation.getPrefix();
    }

    /**
     * All URL components that should be persistent when navigating the given navigator.
     *
     * @param nav the navigator.
     * @param extraParameters any extra parameters that should go into the URL.
     *
     * @param newValue
     * @return the set of parameter names.
     */
    protected final Set<String> getUrlComponentNames(
            final NavigationConfig.Nav nav,
            final Set<String> extraParameters,
            final String newValue) {

        final Set<String> navState = state.getParameterNames(nav, newValue == null || newValue.length() == 0);

        navState.addAll(nav.getStaticParameters().keySet());
        navState.addAll(extraParameters);

        if (!nav.getNavigation().isExcludeQuery()) {
            navState.add("q");
        }

        navState.add("c");

        return navState;
    }

    /**
     * The value for the given URL component.
     *
     * @param nav the navigator
     * @param componentName the name of the url component.
     *
     * @param extraParameters
     * @return the value. UTF-8 URL ENCODED.
     */
    protected String getUrlComponentValue(
            final NavigationConfig.Nav nav,
            final String componentName,
            final Map<String, String> extraParameters) {

        // return the first non-null value found.
        return null != extraParameters.get(componentName)
                ? /*return*/ extraParameters.get(componentName)
                : null != nav.getStaticParameters().get(componentName)
                    ? /*return*/ nav.getStaticParameters().get(componentName)
                    : /*return*/ state.getParameterValue(nav, componentName);
    }

    /**
     * Returns the data model.
     *
     * @return the datamodel.
     */
    protected final DataModel getDataModel() {
        return dataModel;
    }

}
