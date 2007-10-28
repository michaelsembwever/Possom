/*
 * Copyright (2007) Schibsted Søk
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

import java.util.Map;

/**
 * Description.
 *
 * You should probably not implement this interface directly but extend the AbstractUrlGenerator instead.
 */
public interface UrlGenerator {
    /**
     * Returns the url for the given navigator when navigated on the given value.
     *
     * @param value the value to navigate to.
     * @param nav the navigator to navigate.
     *
     * @return the URL.
     */
    String getURL(String value, NavigationConfig.Nav nav);

    /**
     * Returns the url for the given navigator. Also include the supplied extra parameters.
     *
     * @param value the value to navigate to.
     * @param nav the navigator to navigate.
     *
     * @return the URL including given parameters.
     */
    String getURL(String value, NavigationConfig.Nav nav, Map<String, String> extraParameters);
}
