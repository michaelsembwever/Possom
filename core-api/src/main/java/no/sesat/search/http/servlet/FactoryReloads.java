/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
/*
 * FactoryReloads.java
 *
 * Created on 5 May 2006, 07:58
 *
 */

package no.sesat.search.http.servlet;

import java.util.Locale;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.mode.SearchModeFactory;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.query.analyser.AnalysisRuleFactory;
import no.sesat.search.query.token.RegExpEvaluatorFactory;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactory;
import no.sesat.search.view.velocity.VelocityEngineFactory;
import no.sesat.search.view.SearchTabFactory;
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
    @SuppressWarnings("fallthrough")
    public static void performReloads(
            final SiteContext genericCxt,
            final ReloadArg reload){

        final Site site = genericCxt.getSite();

        switch(reload){
            case ALL:
            case SITE_CONFIGURATION:
        
                performReload(site, SiteConfiguration.instanceOf(
                        ContextWrapper.wrap(SiteConfiguration.Context.class, genericCxt)));
                if(ReloadArg.ALL != reload){ break;}
        
            case SEARCH_TAB_FACTORY:
                
                performReload(site, SearchTabFactory.instanceOf(
                        ContextWrapper.wrap(SearchTabFactory.Context.class, genericCxt)));
                if(ReloadArg.ALL != reload){ break;}
                
            case SEARCH_MODE_FACTORY:

                performReload(site, SearchModeFactory.instanceOf(
                        ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt)));
                if( ReloadArg.ALL != reload){ break;}
        
            case ANALYSIS_RULES_FACTORY:
                
                performReload(site, AnalysisRuleFactory.instanceOf(
                        ContextWrapper.wrap(AnalysisRuleFactory.Context.class, genericCxt)));
        
            case REG_EXP_EVALUATOR_FACTORY:
                
                performReload(site, RegExpEvaluatorFactory.instanceOf(
                        ContextWrapper.wrap(RegExpEvaluatorFactory.Context.class, genericCxt)));
                if(ReloadArg.ALL != reload){ break;}
        
            case VELOCITY_ENGINE_FACTORY:
                
                performReload(site, VelocityEngineFactory.instanceOf(
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
