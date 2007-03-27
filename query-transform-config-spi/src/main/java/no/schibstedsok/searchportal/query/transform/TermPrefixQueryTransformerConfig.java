/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;

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
