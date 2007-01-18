/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

/**
 *
 * An implementation of Search Configuration for yellow searches.
 *
 * @author <a href="larsj@conduct.no">Lars Johansson</a>
 * @version $Revision: 1 $
 */
public class CatalogueAdsSearchConfiguration extends FastSearchConfiguration {

    public CatalogueAdsSearchConfiguration(){
        super(null);
    }

    public CatalogueAdsSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

}
