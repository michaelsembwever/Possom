/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Configuration bean for RegexpQueryTransformer.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 *
 */
@Controller("RegexpQueryTransformer")
public final class RegexpQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private static final Logger LOG = Logger.getLogger(RegexpQueryTransformerConfig.class);

    /**
     * Holds value of property regexp.
     */
    private String regexp;

    /**
     * Holds value of property replacement.
     */
    private String replacement = "";

    @Override
    public RegexpQueryTransformerConfig readQueryTransformer(final Element qt){

        super.readQueryTransformer(qt);
        AbstractDocumentFactory.fillBeanProperty(this, null, "regexp", ParseType.String, qt, "");
        AbstractDocumentFactory.fillBeanProperty(this, null, "replacement", ParseType.String, qt, "");
        return this;
    }

    /**
     * Setter for property regexp.
     * @param regexp New value of property regexp.
     */
    public void setRegexp(final String regexp) {
        this.regexp = regexp;
    }

    /**
     *
     * @return
     */
    public String getRegexp(){
        return regexp;
    }

    /**
     * Setter for property replacement.
     * @param replacement New value of property replacement.
     */
    public void setReplacement(final String replacement) {
        this.replacement = replacement;
    }

    /**
     *
     * @return
     */
    public String getReplacement(){
        return replacement;
    }
}
