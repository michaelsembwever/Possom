/* Copyright (2007-2008) Schibsted ASA
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
/*
 * BlendingNewsCommandConfig.java
 *
 * Created on May 12, 2006, 2:20 PM
 */

package no.sesat.search.mode.config;

import java.util.ArrayList;
import java.util.List;
import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import org.w3c.dom.Element;

/**
 *
 *
 * @version $Id$
 */
@Controller("BlendingNewsSearchCommand")
public final class BlendingNewsCommandConfig extends  NewsCommandConfig {

    private List<String> filtersToBlend;
    private int documentsPerFilter;

    /**
     *
     * @return
     */
    public int getDocumentsPerFilter() {
        return documentsPerFilter;
    }

    /**
     *
     * @return
     */
    public List<String> getFiltersToBlend() {
        return filtersToBlend;
    }

    /**
     *
     * @param documentsPerFilter
     */
    public void setDocumentsPerFilter(final int documentsPerFilter) {
        this.documentsPerFilter = documentsPerFilter;
    }

    /**
     *
     * @param filtersToBlend
     */
    public void setFiltersToBlend(final List<String> filtersToBlend) {
        this.filtersToBlend = filtersToBlend;
    }
}
