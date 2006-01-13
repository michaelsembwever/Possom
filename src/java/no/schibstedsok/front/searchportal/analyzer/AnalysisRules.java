// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class AnalysisRules {

    private static final Log LOG = LogFactory.getLog(AnalysisRules.class);
    private static final Map RULES = new HashMap();

    static {

        // [TODO] Move into an xml configuration file. Ensure forward compatibility w/ neural-network design.

        // Common joined predicates
        final Predicate geo = PredicateUtils.orPredicate(TokenPredicate.GEOLOCAL, TokenPredicate.GEOGLOBAL);
        final Predicate geoExact =  PredicateUtils.orPredicate(TokenPredicate.GEOLOCALEXACT, TokenPredicate.GEOGLOBALEXACT);

        final Predicate exactFirstOrLast =  PredicateUtils.orPredicate(TokenPredicate.EXACTFIRST, TokenPredicate.EXACTLAST);
        final Predicate firstAndLastName =  PredicateUtils.andPredicate(TokenPredicate.FIRSTNAME, TokenPredicate.LASTNAME);
        final Predicate firstOrLast =  PredicateUtils.orPredicate(TokenPredicate.FIRSTNAME, TokenPredicate.LASTNAME);
        final Predicate firstOrLastAndGeo =  PredicateUtils.andPredicate(firstOrLast, geo);        
        
        // Person
        final AnalysisRule person = new AnalysisRule();

        final Predicate[] aa = {
                TokenPredicate.EXACTWIKI,
                TokenPredicate.COMPANYSUFFIX,
                TokenPredicate.KEYWORD,
                TokenPredicate.CATEGORY,
                PredicateUtils.andPredicate(TokenPredicate.PRIOCOMPANYNAME,
                        PredicateUtils.notPredicate(TokenPredicate.FIRSTNAME))
        };

        final Predicate notWikiNotCompanyPostfix = PredicateUtils.nonePredicate(aa);
        final Predicate firstOrLastNotCompany = PredicateUtils.andPredicate(notWikiNotCompanyPostfix, firstOrLastAndGeo);

        final Predicate w = PredicateUtils.andPredicate(notWikiNotCompanyPostfix, firstAndLastName);


        final Predicate catalogueAndName = PredicateUtils.andPredicate(TokenPredicate.CATALOGUEPREFIX, firstOrLast);
        final Predicate whiteBoost = PredicateUtils.anyPredicate(new Predicate[] {
            firstOrLastNotCompany,
            w, 
            catalogueAndName
        });


        person.addPredicateScore(TokenPredicate.FULLNAME, 10);
        person.addPredicateScore(w, 90);
        person.addPredicateScore(firstOrLastNotCompany, 90);
        person.addPredicateScore(geoExact, -500);
        person.addPredicateScore(TokenPredicate.PHONENUMBER, 150);
        person.addPredicateScore(catalogueAndName, 150);
        final Predicate[] personOtherPrefixes = {
            TokenPredicate.PICTUREPREFIX,
            TokenPredicate.WEATHERPREFIX,
            TokenPredicate.NEWSPREFIX,
            TokenPredicate.WIKIPEDIAPREFIX,
            TokenPredicate.TVPREFIX
        };

        person.addPredicateScore(PredicateUtils.anyPredicate(personOtherPrefixes), -500);
        person.addPredicateScore(TokenPredicate.TNS, -500);
        person.addPredicateScore(exactFirstOrLast, -500);

        RULES.put("whitePages", person);

        // Company
        final AnalysisRule company = new AnalysisRule();

        final Predicate categoryOrKeyword = PredicateUtils.orPredicate(TokenPredicate.CATEGORY, TokenPredicate.KEYWORD);
        company.addPredicateScore(categoryOrKeyword, 100);

        final Predicate[] g = {
            TokenPredicate.COMPANYSUFFIX,
            TokenPredicate.KEYWORD,
            TokenPredicate.CATEGORY,
            TokenPredicate.PRIOCOMPANYNAME
        };

        company.addPredicateScore(
                PredicateUtils.andPredicate(TokenPredicate.EXACTWIKI, PredicateUtils.nonePredicate(g)), -500);
        final Predicate companyNotPerson = PredicateUtils.andPredicate(
                PredicateUtils.notPredicate(firstAndLastName), TokenPredicate.COMPANYNAME);
        company.addPredicateScore(companyNotPerson, 200);
        company.addPredicateScore(PredicateUtils.andPredicate(firstAndLastName, TokenPredicate.COMPANYSUFFIX), 200);
        company.addPredicateScore(TokenPredicate.PRIOCOMPANYNAME, 30);
        company.addPredicateScore(geoExact, -500);
        company.addPredicateScore(TokenPredicate.ORGNR, 120);
        company.addPredicateScore(TokenPredicate.PHONENUMBER, 200);
        company.addPredicateScore(PredicateUtils.allPredicate(new  Predicate[] {
                PredicateUtils.notPredicate(TokenPredicate.PRIOCOMPANYNAME),
                PredicateUtils.notPredicate(TokenPredicate.COMPANYSUFFIX), firstOrLastAndGeo,
                PredicateUtils.notPredicate(categoryOrKeyword) }), -500);
        company.addPredicateScore(PredicateUtils.allPredicate(new  Predicate[] {
                TokenPredicate.CATALOGUEPREFIX, firstOrLast,
                PredicateUtils.notPredicate(TokenPredicate.COMPANYSUFFIX) }), -500);
        company.addPredicateScore(TokenPredicate.NEWSPREFIX, -500);
        company.addPredicateScore(TokenPredicate.TNS, -500);
        company.addPredicateScore(TokenPredicate.EXACTFIRST, -500);

        RULES.put("yellowPages", company);

        final AnalysisRule globalEnrichment = new AnalysisRule();

        final Predicate[] ppp = {
            TokenPredicate.KEYWORD,
            TokenPredicate.CATEGORY,
            TokenPredicate.FIRSTNAME,
            TokenPredicate.LASTNAME,
            TokenPredicate.COMPANYNAME
        };

        globalEnrichment.addPredicateScore(PredicateUtils
                .andPredicate(TokenPredicate.ENGLISHWORDS, PredicateUtils.notPredicate(TokenPredicate.WIKIPEDIA)), 90);
        globalEnrichment.addPredicateScore(PredicateUtils.anyPredicate(ppp), -500);
        RULES.put("globalEnrichment", globalEnrichment);

        final AnalysisRule picSearch = new AnalysisRule();

        final Predicate[] picOtherPrefixes = {
            TokenPredicate.EXACTCOMPANYNAME,
            TokenPredicate.CATALOGUEPREFIX,
            TokenPredicate.WEATHERPREFIX,
            TokenPredicate.PICTUREPREFIX,
            TokenPredicate.NEWSPREFIX,
            TokenPredicate.WIKIPEDIAPREFIX
        };
        final Predicate picNotOtherPrefixes = PredicateUtils.nonePredicate(picOtherPrefixes);

        picSearch.addPredicateScore(PredicateUtils.andPredicate(picNotOtherPrefixes, TokenPredicate.EXACTWIKI), 550);
        picSearch.addPredicateScore(TokenPredicate.PICTUREPREFIX, 550);
        picSearch.addPredicateScore(geoExact, -600);

        RULES.put("picSearch", picSearch);

        final AnalysisRule wwikipedia = new AnalysisRule();

        final Predicate wikiAndCompany = PredicateUtils.andPredicate(TokenPredicate.PRIOCOMPANYNAME, TokenPredicate.WIKIPEDIA);

        final Predicate[] wikiOtherPrefixes = {
            TokenPredicate.CATALOGUEPREFIX,
            TokenPredicate.WEATHERPREFIX,
            TokenPredicate.PICTUREPREFIX,
            TokenPredicate.NEWSPREFIX
        };

        final Predicate notOtherPrefixes = PredicateUtils.nonePredicate(wikiOtherPrefixes);

        wwikipedia.addPredicateScore(
                PredicateUtils.andPredicate(TokenPredicate.WIKIPEDIAPREFIX, TokenPredicate.WIKIPEDIA), 400);
        wwikipedia.addPredicateScore(
                PredicateUtils.andPredicate(wikiAndCompany, notOtherPrefixes), -500);
        wwikipedia.addPredicateScore(TokenPredicate.EXACTWIKI, 300);
        wwikipedia.addPredicateScore(firstAndLastName, -220);
        wwikipedia.addPredicateScore(geoExact, -220);
        wwikipedia.addPredicateScore(TokenPredicate.TNS, -220);

        RULES.put("wikipedia", wwikipedia);

        final Predicate[] lots = {
            TokenPredicate.TNS,
            TokenPredicate.EXACTWIKI,
            geo,
            TokenPredicate.KEYWORD,
            TokenPredicate.CATEGORY,
            firstOrLast
        };

        final AnalysisRule news = new AnalysisRule();

        final Predicate[] pp = {
            firstAndLastName,
            TokenPredicate.EXACTWIKI,
            PredicateUtils.notPredicate(TokenPredicate.COMPANYSUFFIX)}; // and not big sites.

        news.addPredicateScore(PredicateUtils.andPredicate(TokenPredicate.PRIOCOMPANYNAME,
                PredicateUtils.neitherPredicate(TokenPredicate.FIRSTNAME, geo)), 400);
        news.addPredicateScore(PredicateUtils.allPredicate(pp), 300);
        news.addPredicateScore(TokenPredicate.NEWSPREFIX, 400);
        news.addPredicateScore(PredicateUtils.truePredicate(), 100);
        news.addPredicateScore(PredicateUtils.andPredicate(
                PredicateUtils.notPredicate(TokenPredicate.NEWSPREFIX), categoryOrKeyword), -550);
        news.addPredicateScore(
                PredicateUtils.andPredicate(TokenPredicate.COMPANYNAME,
                PredicateUtils.notPredicate(TokenPredicate.PRIOCOMPANYNAME)), -20);
        news.addPredicateScore(PredicateUtils.orPredicate(geoExact, whiteBoost), -20);
        news.addPredicateScore(PredicateUtils.orPredicate(TokenPredicate.TNS, companyNotPerson), -20);
        news.addPredicateScore(TokenPredicate.CATALOGUEPREFIX, -500);
        news.addPredicateScore(
                PredicateUtils.allPredicate(new  Predicate[] {
            PredicateUtils.notPredicate(firstAndLastName), TokenPredicate.EXACTWIKI
        }), -20);
        news.addPredicateScore(TokenPredicate.WIKIPEDIAPREFIX, -500);
        news.addPredicateScore(TokenPredicate.WEATHERPREFIX, -500);
        news.addPredicateScore(
                PredicateUtils.andPredicate(TokenPredicate.ENGLISHWORDS, PredicateUtils.nonePredicate(lots)), -500);

        news.addPredicateScore(PredicateUtils.andPredicate(
                PredicateUtils.neitherPredicate(TokenPredicate.COMPANYNAME, TokenPredicate.EXACTWIKI),
                TokenPredicate.EXACTFIRST), -500);

        RULES.put("news", news);


        final AnalysisRule tv = new AnalysisRule();

        final Predicate[] otherPrefTv = {
            TokenPredicate.CATALOGUEPREFIX,
            TokenPredicate.WIKIPEDIAPREFIX,
            TokenPredicate.PICTUREPREFIX,
            TokenPredicate.WEATHERPREFIX
        };

        tv.addPredicateScore(PredicateUtils.anyPredicate(otherPrefTv), -500);
        tv.addPredicateScore(TokenPredicate.TVPREFIX, 1000);
        tv.addPredicateScore(PredicateUtils.notPredicate(geo), 500);
        tv.addPredicateScore(TokenPredicate.NEWSPREFIX, -420);
        tv.addPredicateScore(categoryOrKeyword, -420);
        tv.addPredicateScore(TokenPredicate.TNS, -500);
        RULES.put("tv", tv);

        // Global search
        final AnalysisRule globalSearch = new AnalysisRule();

        globalSearch.addPredicateScore(
                PredicateUtils.andPredicate(TokenPredicate.ENGLISHWORDS, PredicateUtils.nonePredicate(lots)), 100);
        RULES.put("globalSearch", globalSearch);


        final AnalysisRule weather = new AnalysisRule();
        final Predicate[] allCompanyAndPresonHits = {
            TokenPredicate.FULLNAME,
            TokenPredicate.KEYWORD,
            TokenPredicate.CATEGORY,
            TokenPredicate.COMPANYNAME
        };
        weather.addPredicateScore(PredicateUtils.andPredicate(TokenPredicate.WEATHERPREFIX, geo), 400);
        weather.addPredicateScore(geoExact, 500);
        weather.addPredicateScore(PredicateUtils.andPredicate(PredicateUtils
                .notPredicate(geo), PredicateUtils
                .anyPredicate(allCompanyAndPresonHits)), -500);
        weather.addPredicateScore(TokenPredicate.TNS, -500);
        RULES.put("weather", weather);

        final AnalysisRule mathExpression = new AnalysisRule();
        mathExpression.addPredicateScore(TokenPredicate.MATHPREDICATE, 500);
        RULES.put("mathExpression", mathExpression);

    }

    /**
     * 
     * Returns a map of all the RULES. The key is the name of the rule
     * 
     * @return all RULES.
     */
    public static Map getRules() {
        return RULES;
    }


    /**
     *
     * Returns the rule with the name <code>ruleName</code>.
     *
     * @param   ruleName    the name of the rule
     * @return  the rule.
     */
    public static AnalysisRule getRule(final String ruleName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: getRule()" + ruleName);
        }
        final AnalysisRule rule = (AnalysisRule) RULES.get(ruleName);

        return rule;
    }
}
