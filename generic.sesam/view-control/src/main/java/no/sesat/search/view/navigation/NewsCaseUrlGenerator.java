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

import no.sesat.search.datamodel.DataModel;

import java.util.*;

/**
 * A UrlGenerator for the news aggregator. Generates URL on the format /search/nyheter/person/Al+Gore.
 *
 * @author maek
 */
public final class NewsCaseUrlGenerator extends BasicUrlGenerator {

    private static final List<String> PATH_COMPONENTS = Arrays.asList("type", "newsCase");

    public NewsCaseUrlGenerator(DataModel dataModel, NavigationConfig.Navigation navigation, NavigationState state) {
        super(dataModel, navigation, state);
    }

    @Override
    protected void appendParameterComponent(final String parameter, final String value) {
        if (! ("c".equals(parameter) || "q".equals(parameter))) {
            super.appendParameterComponent(parameter, value);
        }
    }
    @Override
    protected List<String> getPathComponents(NavigationConfig.Nav nav) {
        return PATH_COMPONENTS;
    }
}
