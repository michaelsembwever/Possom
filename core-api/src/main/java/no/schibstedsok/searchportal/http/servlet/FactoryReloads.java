/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
/*
 * FactoryReloads.java
 *
 * Created on 5 May 2006, 07:58
 *
 */

package no.schibstedsok.searchportal.http.servlet;

import java.util.Locale;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.SearchModeFactory;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.query.analyser.AnalysisRuleFactory;
import no.schibstedsok.searchportal.query.token.RegExpEvaluatorFactory;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.view.velocity.VelocityEngineFactory;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;
import org.apache.log4j.Logger;

/** Utility class to remove factory instances for a given Site and its locale derivatives.
 * The factory class to clean instances from is indicated by the value of ReloadArg.
 * Also performs a System.gc() to clean out WeakReference caches.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class FactoryReloads {

    
    public enum ReloadArg{
        ALL,
        SITE_CONFIGURATION,
        SEARCH_TAB_FACTORY,
        SEARCH_MODE_FACTORY,
        ANALYSIS_RULES_FACTORY,
        REG_EXP_EVALUATOR_FACTORY,
        VELOCITY_ENGINE_FACTORY
    }

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FactoryReloads.class);

    private static final String WARN_CLEANED_1 = " on cleaning ";
    private static final String WARN_CLEANED_2 = " (against all locales) for ";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    /** Remove factory instances for a given Site and its locale derivatives.
     * The factory class to clean instances from is indicated by the value of ReloadArg. 
     * Also performs a System.gc() to clean out WeakReference caches.
     **/
    public static void performReloads(
            final SiteContext genericCxt,
            final ReloadArg reload){

        final Site site = genericCxt.getSite();

        switch(reload){
            case ALL:
            case SITE_CONFIGURATION:
        
                performReload(site, SiteConfiguration.valueOf(
                        ContextWrapper.wrap(SiteConfiguration.Context.class, genericCxt)));
                if(ReloadArg.ALL != reload){ break;}
        
            case SEARCH_TAB_FACTORY:
                
                performReload(site, SearchTabFactory.valueOf(
                        ContextWrapper.wrap(SearchTabFactory.Context.class, genericCxt)));
                if(ReloadArg.ALL != reload){ break;}
                
            case SEARCH_MODE_FACTORY:

                performReload(site, SearchModeFactory.valueOf(
                        ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt)));
                if( ReloadArg.ALL != reload){ break;}
        
            case ANALYSIS_RULES_FACTORY:
                
                performReload(site, AnalysisRuleFactory.valueOf(
                        ContextWrapper.wrap(AnalysisRuleFactory.Context.class, genericCxt)));
        
            case REG_EXP_EVALUATOR_FACTORY:
                
                performReload(site, RegExpEvaluatorFactory.valueOf(
                        ContextWrapper.wrap(RegExpEvaluatorFactory.Context.class, genericCxt)));
                if(ReloadArg.ALL != reload){ break;}
        
            case VELOCITY_ENGINE_FACTORY:
                
                performReload(site, VelocityEngineFactory.valueOf(
                        ContextWrapper.wrap(VelocityEngineFactory.Context.class, genericCxt)));
                if(ReloadArg.ALL != reload){ break;}
            
        }
        
        // clean out WeakReference caches
        System.gc();
    }
    
    private static void performReload(
            final Site site,
            final SiteKeyedFactory factory){
        
        LOG.warn(removeAllLocalesFromSiteKeyedFactory(site, factory) 
                + WARN_CLEANED_1 + factory.getClass().getSimpleName() + WARN_CLEANED_2 + site);
    }

    private static int removeAllLocalesFromSiteKeyedFactory(
            final Site site,
            final SiteKeyedFactory factory){

        int cleaned = 0;
        for(Locale l : Locale.getAvailableLocales()){
            final Site s = Site.valueOf(null, site.getName(), l);
            if(null != s && factory.remove(site)){
                ++cleaned;
            }
        }
        return cleaned;
    }

    // Constructors --------------------------------------------------

    private FactoryReloads(){}


    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
    
}
