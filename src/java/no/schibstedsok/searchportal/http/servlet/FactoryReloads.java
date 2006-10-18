// Copyright (2006) Schibsted Søk AS
/*
 * FactoryReloads.java
 *
 * Created on 5 May 2006, 07:58
 *
 */

package no.schibstedsok.searchportal.http.servlet;

import java.util.Locale;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.SearchModeFactory;
import no.schibstedsok.searchportal.mode.config.SiteConfiguration;
import no.schibstedsok.searchportal.query.analyser.AnalysisRuleFactory;
import no.schibstedsok.searchportal.query.token.RegExpEvaluatorFactory;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.view.velocity.VelocityEngineFactory;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class FactoryReloads {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FactoryReloads.class);

    private static final String WARN_TABS_CLEANED = " on cleaning tabs (against all locales) for ";
    private static final String WARN_CONFIG_CLEANED = " on cleaning configuration (against all locales) for ";
    private static final String WARN_MODES_CLEANED = " on cleaning modes (against all locales) for ";
    private static final String WARN_ANALYSIS_CLEANED = " on cleaning AnalysisRules (against all locales) for ";
    private static final String WARN_REGEXP_CLEANED = " on cleaning RegularExpressionEvaluators (against all locales) for ";
    private static final String WARN_VELOCITY_CLEANED = " on cleaning VelocityEngines (against all locales) for ";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    /** TODO comment me. **/
    public static void performReloads(
            final SiteContext genericCxt,
            final String reload){

        final Site site = genericCxt.getSite();

        if("all".equalsIgnoreCase(reload) || "configuration".equalsIgnoreCase(reload)){
            LOG.warn(removeAllLocalesFromSiteKeyedFactory(site,
                    SiteConfiguration.valueOf(ContextWrapper.wrap(SiteConfiguration.Context.class, genericCxt)))
                    + WARN_CONFIG_CLEANED + site);
        }
        if("all".equalsIgnoreCase(reload) || "views".equalsIgnoreCase(reload)){
            LOG.warn(removeAllLocalesFromSiteKeyedFactory(site,
                    SearchTabFactory.valueOf(ContextWrapper.wrap(SearchTabFactory.Context.class, genericCxt)))
                     + WARN_TABS_CLEANED + site);
        }
        if("all".equalsIgnoreCase(reload) || "modes".equalsIgnoreCase(reload)){
            LOG.warn(removeAllLocalesFromSiteKeyedFactory(site,
                    SearchModeFactory.valueOf(ContextWrapper.wrap(SearchModeFactory.Context.class, genericCxt)))
                     + WARN_MODES_CLEANED + site);
        }
        if("all".equalsIgnoreCase(reload) || "AnalysisRules".equalsIgnoreCase(reload)){
            LOG.warn(removeAllLocalesFromSiteKeyedFactory(site,
                    AnalysisRuleFactory.valueOf(ContextWrapper.wrap(AnalysisRuleFactory.Context.class, genericCxt)))
                     + WARN_ANALYSIS_CLEANED + site);
        }
        if("all".equalsIgnoreCase(reload) || "RegularExpressionEvaluators".equalsIgnoreCase(reload)){
            LOG.warn(removeAllLocalesFromSiteKeyedFactory(site,
                    RegExpEvaluatorFactory.valueOf(ContextWrapper.wrap(RegExpEvaluatorFactory.Context.class, genericCxt)))
                     + WARN_REGEXP_CLEANED + site);
        }
        if("all".equalsIgnoreCase(reload) || "velocity".equalsIgnoreCase(reload)){
            LOG.warn(removeAllLocalesFromSiteKeyedFactory(site,
                    VelocityEngineFactory.valueOf(ContextWrapper.wrap(VelocityEngineFactory.Context.class, genericCxt)))
                     + WARN_VELOCITY_CLEANED + site);
        }
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
