// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * TvSearcQueryTransformer is part of no.schibstedsok.searchportal.query
 *
 * @author ajamtli
 * @version $Id$
 */
@Controller("TvsearchQueryTransformer")
public final class TvsearchQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private boolean withEndtime;

    /** TODO comment me. *
     * @return
     */
    public boolean getWithEndtime() {
        return withEndtime;
    }

    /** TODO comment me. *
     * @param withEndtime
     */
    public void setWithEndtime(final boolean withEndtime) {
        this.withEndtime = withEndtime;
    }

    @Override
    public TvsearchQueryTransformerConfig readQueryTransformer(final Element qt){

        super.readQueryTransformer(qt);
        AbstractDocumentFactory.fillBeanProperty(this, null, "withEndtime", ParseType.Boolean, qt, "");
        return this;
    }
}
