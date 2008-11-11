/* Copyright (2008) Schibsted Søk AS
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
package no.sesat.search.mode.config.querybuilder;

import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/** Prefix QueryBuilder.
 *
 * @version $Id$
 */
@Controller("PrefixQueryBuilder")
public class PrefixQueryBuilderConfig extends AbstractQueryBuilderConfig{

    private String orPrefix = "";
    private String andPrefix = "+";
    private String defaultPrefix = "";

    public String getOrPrefix(){
        return orPrefix;
    }

    public void setOrPrefix(final String or){
        orPrefix = or;
    }

    public String getAndPrefix(){
        return andPrefix;
    }

    public void setAndPrefix(final String and){
        andPrefix = and;
    }

    public String getDefaultPrefix(){
        return defaultPrefix;
    }

    public void setDefaultPrefix(final String defaultPrefix){
        this.defaultPrefix = defaultPrefix;
    }

    @Override
    public PrefixQueryBuilderConfig readQueryBuilder(final Element element) {

        final PrefixQueryBuilderConfig config = (PrefixQueryBuilderConfig) super.readQueryBuilder(element);
        AbstractDocumentFactory.fillBeanProperty(config, null, "orPrefix", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(config, null, "andPrefix", ParseType.String, element, "+");
        AbstractDocumentFactory.fillBeanProperty(config, null, "defaultPrefix", ParseType.String, element, "");
        return config;
    }
}
