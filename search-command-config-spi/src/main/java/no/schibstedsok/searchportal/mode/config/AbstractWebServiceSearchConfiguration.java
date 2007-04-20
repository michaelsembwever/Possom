// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;



/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id $</tt>
 */
public abstract class AbstractWebServiceSearchConfiguration extends AbstractSearchConfiguration {

    /**
     * 
     */
    public AbstractWebServiceSearchConfiguration(){
        super(null);
    }
    
    /**
     * 
     * @param asc 
     */
    public AbstractWebServiceSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }



}
