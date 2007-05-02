/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 *
 * An implementation of Search Configuration for catalogue banner search.
 *
 * Injected by SearchModeFactory with value from modes.xml,
 * by the fillBeanProperty pattern.
 *
 * @author Stian Hegglund
 * @version $Id$
 */
@Controller("CatalogueBannersSearchCommand")
public final class CatalogueBannersCommandConfig extends FastCommandConfig {

    /** The name of the parameter which holds the geographic user supplied location.*/
    private String queryParameterWhere;

    /**
     *  getter for queryParameterWhere
     * @return
     */
    public String getQueryParameterWhere() {
            return queryParameterWhere;
    }

    /**
     * Injected by SearchModeFactory with value from modes.xml,
     * by the fillBeanProperty pattern.
     * @param queryParameterWhere
     */
    public void setQueryParameterWhere(String queryParameterWhere) {
            this.queryParameterWhere = queryParameterWhere;
    }

    @Override
    public FastCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {

        super.readSearchConfiguration(element, inherit);

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "queryParameterWhere", ParseType.String, element, "");

        return this;
    }


}
