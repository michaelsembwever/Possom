// Copyright (2006) Schibsted Søk AS
/*
 * WhiteSearchConfiguration.java
 *
 * Created on March 6, 2006, 4:14 PM
 *
 */

package no.schibstedsok.searchportal.configuration;

/**
 *
 * @author magnuse
 */
public class WhiteSearchConfiguration extends FastSearchConfiguration {

    public WhiteSearchConfiguration(){
        super(null);
    }

    public WhiteSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
