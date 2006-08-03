/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.configuration;

/**
 *
 * An implementation of Search Configuration for yellow searches.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public class YellowSearchConfiguration extends FastSearchConfiguration {

    public YellowSearchConfiguration(){
        super(null);
    }

    public YellowSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

}
