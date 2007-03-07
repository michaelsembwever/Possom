/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

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
