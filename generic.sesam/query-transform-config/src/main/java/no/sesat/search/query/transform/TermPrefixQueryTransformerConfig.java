/*
 * Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
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
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;

import org.w3c.dom.Element;

/**
 * A transformer to prefix the terms in a query with a named field.
 *
 * Works like <regexp regexp="$" replacement="/prefix/"/>
 *  except that it
 *   avoids adding the prefix to clauses that already have fields,
 *   allows seperate configured prefixes for LeafClauses, IntegerClauses,
 * and PhoneNumberPrefix, UrlClauses, and EmailClauses.
 *
 * Multiple prefixes can be configured with comma seperation.
 * EG prefix="site,domain"
 * By default this writes out (site:term  domain:term)
 * but the joining operator can be configured with #setMultiTermJoin(string)
 *
 * @version $Id$
 */
@Controller("TermPrefixQueryTransformer")
public final class TermPrefixQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private String urlPrefix;
    private String emailPrefix;
    private String phoneNumberPrefix;
    private String numberPrefix;
    private String prefix;
    private String multiTermJoin;

    /**
     * @see #setPrefix(java.lang.String)
     *
     * @return the prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix to be used for words.
     * @param prefix The prefix to set.
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    /**
     * @see #setNumberPrefix(java.lang.String)
     *
     * @return the numberPrefix.
     */
    public String getNumberPrefix() {
        return numberPrefix;
    }

    /**
     * Set the prefix to used for numbers.
     * If not defined falls back to value of prefix
     *
     * @param numberPrefix The prefix.
     */
    public void setNumberPrefix(final String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    /**
     * @see #setPhoneNumberPrefix(java.lang.String)
     *
     * @return the phoneNumberPrefix.
     */
    public String getPhoneNumberPrefix() {
        return phoneNumberPrefix;
    }

    /**
     * Set the prefix to used for numbers.
     * If not defined falls back to value of numberPreix
     *
     * @param phoneNumberPrefix The prefix.
     */
    public void setPhoneNumberPrefix(final String phoneNumberPrefix) {
        this.phoneNumberPrefix = phoneNumberPrefix;
    }

    /**
     * @see #setUrlPrefix(java.lang.String)
     *
     * @return the prefix.
     */
    public String getUrlPrefix() {
        return urlPrefix;
    }

    /**
     * Set the prefix to be used for UrlClauses.
     * @param prefix The prefix to set.
     */
    public void setUrlPrefix(final String prefix) {
        this.urlPrefix = prefix;
    }

    /**
     * @see #setEmailPrefix(java.lang.String)
     *
     * @return the prefix.
     */
    public String getEmailPrefix() {
        return emailPrefix;
    }

    /**
     * Set the prefix to be used for EmailClauses.
     * @param prefix The prefix to set.
     */
    public void setEmailPrefix(final String prefix) {
        this.emailPrefix = prefix;
    }

    /**
     * @see #setMultiTermJoin(java.lang.String)
     *
     * @return the multiTermJoin.
     */
    public String getMultiTermJoin() {
        return multiTermJoin;
    }

    /**
     * Set the multiTermJoin.
     * @param multiTermJoin The multiTermJoin.
     */
    public void setMultiTermJoin(final String multiTermJoin) {
        this.multiTermJoin = multiTermJoin;
    }

    @Override
    public TermPrefixQueryTransformerConfig readQueryTransformer(final Element qt){

        super.readQueryTransformer(qt);
        AbstractDocumentFactory.fillBeanProperty(this, null, "prefix", ParseType.String, qt, "");
        AbstractDocumentFactory.fillBeanProperty(this, null, "numberPrefix", ParseType.String, qt, getPrefix());
        AbstractDocumentFactory.fillBeanProperty(this, null, "phoneNumberPrefix", ParseType.String, qt, getNumberPrefix());
        AbstractDocumentFactory.fillBeanProperty(this, null, "urlPrefix", ParseType.String, qt, getPrefix());
        AbstractDocumentFactory.fillBeanProperty(this, null, "emailPrefix", ParseType.String, qt, getPrefix());
        AbstractDocumentFactory.fillBeanProperty(this, null, "multiTermJoin", ParseType.String, qt, "");
        return this;
    }
}
