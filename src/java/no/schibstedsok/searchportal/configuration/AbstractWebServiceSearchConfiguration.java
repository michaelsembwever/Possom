// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.configuration;

import no.schibstedsok.searchportal.command.PicSearchCommand;
import no.schibstedsok.searchportal.command.SearchCommand;
import no.schibstedsok.searchportal.query.run.RunningQuery;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id $</tt>
 */
public abstract class AbstractWebServiceSearchConfiguration extends AbstractSearchConfiguration {

    public AbstractWebServiceSearchConfiguration(){
        super(null);
    }
    
    public AbstractWebServiceSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }



}
