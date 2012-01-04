/* Copyright (2012) Schibsted ASA
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
package no.sesat.search.query.transform;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;

@Controller("NewsMyNewsQueryTransformer")
public class NewsMyNewsQueryTransformerConfig extends AbstractQueryTransformerConfig {
    private String filterField;
    private String type;
    private static final String TYPE = "type";
    private static final String FILTER_FIELD = "filter-field";
    private static final String POSITION = "position";
    private static final String QUERY_PARAMETER = "query-parameter";
    private int position = -1;
    private String queryParameter;

    public String getFilterField() {
        return filterField;
    }

    public void setFilterField(String filterField) {
        this.filterField = filterField;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(String string) {
    	queryParameter = string;
    }

    @Override
    public NewsMyNewsQueryTransformerConfig readQueryTransformer(final Element element) {
        type = element.getAttribute(TYPE);
        queryParameter = element.getAttribute(QUERY_PARAMETER);
        if (element.getAttribute(FILTER_FIELD) != null && element.getAttribute(FILTER_FIELD).length() > 0) {
            filterField = element.getAttribute(FILTER_FIELD);
        }
        if (element.getAttribute(POSITION) != null && element.getAttribute(POSITION).length() > 0) {
            position = Integer.parseInt(element.getAttribute(POSITION));
        }
        return this;
    }

}
