// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.command.PicSearchCommand;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.run.RunningQuery;

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
