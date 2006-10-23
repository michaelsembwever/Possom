// Copyright (2005-2006) Schibsted Søk AS
/*
 * AbstractFactoryTest.java
 *
 * Created on October 14, 2006, 1:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.searchportal.TestCase;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.Site.Context;
import no.schibstedsok.searchportal.site.SiteContext;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractFactoryTest extends TestCase {
    
    /** Creates a new instance of AbstractFactoryTest */
    public AbstractFactoryTest(final String testName) {
        super(testName);
    }

    
    protected Site.Context getSiteConstructingContext(){
        
        return new Context(){
            public String getParentSiteName(final SiteContext siteContext){
                return Site.DEFAULT.getName();
            }
        };
    }
    
}
