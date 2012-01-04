/*
 * Copyright (2007-2012) Schibsted ASA
 *   This file is part of Possom.
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
package no.sesat.search.view.navigation;

import java.util.Map;

/**
 *
 * You should probably not implement this interface directly but extend the AbstractUrlGenerator instead.
 *
 *
 * @version $Id: AbstractUrlGenerator.java 6066 2008-01-30 11:27:42Z ssmiweve $
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
     * @param extraParameters
     * @return the URL including given parameters.
     */
    String getURL(String value, NavigationConfig.Nav nav, Map<String, String> extraParameters);
}
