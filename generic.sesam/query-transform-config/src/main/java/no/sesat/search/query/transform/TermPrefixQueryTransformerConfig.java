/*
 * Copyright (2005-2007) Schibsted Søk AS
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
package no.sesat.search.query.transform;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;

import org.w3c.dom.Element;

/**
 * A transformer to prefix the terms in a query.
 *
 * @version $Id$
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 *
 */
@Controller("TermPrefixQueryTransformer")
public final class TermPrefixQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private String numberPrefix;
    private String prefix;

    /**
     * Get the prefix to be used for words.
     *
     * @return the prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the prefix to be used for integers.
     *
     * @return the numberPrefix.
     */
    public String getNumberPrefix() {
        return numberPrefix;
    }

    /**
     * Set the prefix to used for numbers.
     *
     * @param numberPrefix The prefix.
     */
    public void setNumberPrefix(final String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    /**
     * Set the prefix to be used for words.
     * @param prefix The prefix to set.
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public TermPrefixQueryTransformerConfig readQueryTransformer(final Element qt){

        super.readQueryTransformer(qt);
        AbstractDocumentFactory.fillBeanProperty(this, null, "prefix", ParseType.String, qt, "");
        AbstractDocumentFactory.fillBeanProperty(this, null, "numberPrefix", ParseType.String, qt, "");
        return this;
    }
}
