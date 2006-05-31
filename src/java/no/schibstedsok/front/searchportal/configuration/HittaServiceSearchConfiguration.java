// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.command.PicSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id $</tt>
 */
public final class HittaServiceSearchConfiguration extends AbstractWebServiceSearchConfiguration {

    public HittaServiceSearchConfiguration(){
        super(null);
    }
    
    public HittaServiceSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    /**
     * Holds value of property catalog.
     */
    private String catalog;

    /**
     * Getter for property catalog.
     * @return Value of property catalog.
     */
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * Setter for property catalog.
     * @param catalog New value of property catalog.
     */
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }


}
