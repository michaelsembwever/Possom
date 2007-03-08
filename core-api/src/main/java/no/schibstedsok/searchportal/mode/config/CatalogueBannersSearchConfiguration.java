/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

/**
 *
 * An implementation of Search Configuration for catalogue banner search.
 *
 * @author Stian Hegglund
 * @version $Revision:$
 */
public class CatalogueBannersSearchConfiguration extends FastSearchConfiguration {

    public CatalogueBannersSearchConfiguration(){
        super(null);
    }

    public CatalogueBannersSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

}
