// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("MathExpressionSearchCommand")
public class MathExpressionSearchConfiguration extends AbstractSearchConfiguration {

    /**
     * 
     */
    public MathExpressionSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public MathExpressionSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
