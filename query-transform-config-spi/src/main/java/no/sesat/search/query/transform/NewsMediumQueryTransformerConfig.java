/* Copyright (2007) Schibsted ASA
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
package no.sesat.search.query.transform;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;

@Controller("NewsMediumQueryTransformer")
public class NewsMediumQueryTransformerConfig extends AbstractQueryTransformerConfig {
    public static final String ALL_MEDIUMS = "all";
    private String mediumPrefix = "medium";
    private String defaultMedium = "webnewsarticle";
    private String mediumParameter = "medium";


    public String getMediumPrefix() {
        return mediumPrefix;
    }

    public String getDefaultMedium() {
        return defaultMedium;
    }

    public String getMediumParameter() {
        return mediumParameter;
    }

    @Override
    public AbstractQueryTransformerConfig readQueryTransformer(final Element element) {
        String s = element.getAttribute("medium-prefix");
        if (s != null && s.length() > 0) {
            mediumPrefix = s;
        }
        s = element.getAttribute("default-medium");
        if (s != null && s.length() > 0) {
            defaultMedium = s;
        }
        s = element.getAttribute("medium-parameter");
        if (s != null && s.length() > 0) {
            mediumParameter = s;
        }
        return this;
    }
}
