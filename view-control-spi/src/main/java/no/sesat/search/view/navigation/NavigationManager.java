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
import no.sesat.search.datamodel.generic.StringDataObject;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Class responsible for assembling the current navigation state.
 *
 *
 *
 * @version $Id$
 */
public final class NavigationManager {

    private static final Logger LOG = Logger.getLogger(NavigationManager.class);
    private final DataModel dataModel;
    private final Map<String, String> parameters;

    /**
     * Creates a new NavigationManager.
     *
     * @param dataModel The datamodel.
     */
    public NavigationManager(final DataModel dataModel) {
        this.dataModel = dataModel;

        parameters = new HashMap<String, String>();

        for (final NavigationConfig.Navigation n : dataModel.getNavigation().getConfiguration().getNavigationList()) {
            parameters.putAll(new Generator(n).getParameters());
        }
    }

    /**
     * Returns the navigation state that should be persisted for a particular navigator. The state differs between
     * different navigators because there are things like the reset and is-out functionality.
     *
     * @see no.sesat.search.view.navigation.NavigationConfig.Navigation#resetNavSet
     * @see no.sesat.search.view.navigation.NavigationConfig.Nav#isOut
     *
     * @return the navigation state for the given navigator.
     */
    public NavigationState getNavigationState() {
        return new NavigationState() {

            /**
             * Returns the set of parameters that should be kept when navigating the given navigator.
             *
             * @param nav the navigation.
             *
             * @return the set of parameters.
             *
             */
            public Set<String> getParameterNames(final NavigationConfig.Nav nav, final boolean reset) {

                /*
                 * @todo come up with a data structure that does not require us to create new sets.
                 */
                if (nav.getId() != null && !nav.isOut()) {

                    final Set<String> pNames = new HashSet<String>(parameters.keySet());

                    if (! nav.getNavigation().getResetNavSet().isEmpty()) {
                        pNames.removeAll(nav.getNavigation().getResetNavSet());
                    }

                    if (reset) {
                        reset(pNames, nav);
                    } else {
                        for (NavigationConfig.Nav child : nav.getChildNavs()) {
                            reset(pNames, child);
                        }
                    }

                    return pNames;
                } else {
                    return new HashSet<String>(2);
                }
            }

            /**
             * Returns the value for the given navigation parameter.
             *
             * @param nav the navigator.
             * @param parameterName the parameter.
             *
             * @return the value of the parameter. UTF-8 URL ENCODED.
             */
            public String getParameterValue(final NavigationConfig.Nav nav, final String parameterName) {

                final String value = parameters.get(parameterName);

                if (null == value && null != dataModel.getParameters().getValue(parameterName)) {
                    return dataModel.getParameters().getValue(parameterName).getUtf8UrlEncoded();
                }

                return value;
            }

            private void reset(final Set<String> pNames, final NavigationConfig.Nav nav) {
                pNames.remove(nav.getId());
                for (NavigationConfig.Nav child: nav.getChildNavs()) {
                    reset(pNames, child);
                }
            }
        };
    }

    private class Generator {

        private final NavigationConfig.Navigation navigation;
        private final Map<String, String> parameters = new HashMap<String, String>();

        public Generator(final NavigationConfig.Navigation navigation) {
            this.navigation = navigation;
            addNavigationFragments();

        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        private void addNavigationFragments() {

           final Set<String> fieldFilterSet = new HashSet<String>();

            for (final NavigationConfig.Nav nav : navigation.getNavList()) {
                addNavigationFragment(fieldFilterSet, nav);
            }
        }

        private void addNavigationFragment(
                final Set<String> fieldFilterSet,
                final NavigationConfig.Nav nav) {


            StringDataObject fieldValue = dataModel.getParameters().getValue(nav.getField());

            if (!fieldFilterSet.contains(nav.getField())) {
                addPreviousField(fieldValue, nav.getField());
                fieldFilterSet.add(nav.getField());
                for (NavigationConfig.Nav childNav : nav.getChildNavs()) {
                    addNavigationFragment(fieldFilterSet, childNav);
                }
            }
        }

        private void addPreviousField(StringDataObject fieldValue, final String fieldName) {
            if (fieldValue != null) {
                parameters.put(enc(fieldName), enc(fieldValue.getString()));
            }
        }

        private String enc(final String str) {

            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error(e);
                return str;
            }
        }
    }
}
