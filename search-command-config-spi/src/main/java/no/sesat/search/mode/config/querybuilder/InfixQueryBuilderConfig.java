/* Copyright (2008) Schibsted ASA
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

/**
 *
 * @version $Id$
 */
@Controller("InfixQueryBuilder")
public class InfixQueryBuilderConfig extends AbstractQueryBuilderConfig{

    private String orInfix = "OR";
    private String andInfix = "AND";
    private String defaultInfix = "";

    public InfixQueryBuilderConfig(){}

    public InfixQueryBuilderConfig(
            final String orInfix,
            final String andInfix,
            final String defaultInfix,
            final String notPrefix,
            final boolean orGrouped,
            final boolean andGrouped,
            final boolean defaultGrouped){

        super(notPrefix, orGrouped, andGrouped, defaultGrouped);
        this.orInfix = orInfix;
        this.andInfix = andInfix;
        this.defaultInfix = defaultInfix;
    }

    public String getOrInfix(){
        return orInfix;
    }

    public void setOrInfix(final String or){
        orInfix = or;
    }

    public String getAndInfix(){
        return andInfix;
    }

    public void setAndInfix(final String and){
        andInfix = and;
    }

    public String getDefaultInfix(){
        return defaultInfix;
    }

    public void setDefaultInfix(final String defaultInfix){
        this.defaultInfix = defaultInfix;
    }

    @Override
    public InfixQueryBuilderConfig readQueryBuilder(final Element element) {

        final InfixQueryBuilderConfig config = (InfixQueryBuilderConfig) super.readQueryBuilder(element);
        AbstractDocumentFactory.fillBeanProperty(config, null, "orInfix", ParseType.String, element, "OR");
        AbstractDocumentFactory.fillBeanProperty(config, null, "andInfix", ParseType.String, element, "AND");
        AbstractDocumentFactory.fillBeanProperty(config, null, "defaultInfix", ParseType.String, element, "");
        return this;
    }
}
