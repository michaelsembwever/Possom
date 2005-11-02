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
public class AnalysisRules {

    Log log = LogFactory.getLog(AnalysisRules.class);
    static Map rules = new HashMap();

    static {

        // Basic predicates.


        Predicate exactFirst = new TokenPredicate("exact_firstname");
        Predicate exactLast = new TokenPredicate("exact_lastname");

        Predicate exactFirstOrLast = PredicateUtils.orPredicate(exactLast, exactFirst);

        Predicate picture = new TokenPredicate("picture");
        Predicate tns = new TokenPredicate("tns");

        Predicate firstName = new TokenPredicate("firstname");
        Predicate lastName = new TokenPredicate("lastname");
        Predicate companyName = new TokenPredicate("company");
        Predicate exactCompanyName = new TokenPredicate("exact_company");
//        Predicate geo = new TokenPredicate("geo");
        Predicate geoLocal = new TokenPredicate("geolocal");
        Predicate geoGlobal = new TokenPredicate("geoglobal");
        Predicate geoLocalExact = new TokenPredicate("exact_geolocal");
        Predicate geoGlobalExact = new TokenPredicate("exact_geoglobal");
        Predicate category = new TokenPredicate("category");
        Predicate prioCompanyName = new TokenPredicate("companypriority");
        Predicate keyword = new TokenPredicate("keyword");
        Predicate fullName = new TokenPredicate("fullname"); // ?
        Predicate cataloguePrefix = new TokenPredicate("cataloguePrefix");
        Predicate weatherPrefix = new TokenPredicate("weatherPrefix");
        Predicate picturePrefix = new TokenPredicate("picturePrefix");
        Predicate newsPrefix = new TokenPredicate("newsPrefix");
        Predicate exactWiki = new TokenPredicate("exact_wikino");
        Predicate wikipediaPrefix = new TokenPredicate("wikipediaPrefix");
        Predicate orgNr = new TokenPredicate("orgNr");
        Predicate wikipedia = new TokenPredicate("wikino");
        Predicate phoneNumber = new TokenPredicate("phoneNumber");
        Predicate englishWords = new TokenPredicate("international");
        Predicate tvPrefix = new TokenPredicate("tvPrefix");
        Predicate nameLongerThanWikipedia = new TokenPredicate("nameLongerThanWikipedia");
        Predicate globalSearchEnabled = new ParameterPredicate("s", "g");
        Predicate globalSearchNotEnabled = PredicateUtils.notPredicate(globalSearchEnabled);
        Predicate companySuffix = new TokenPredicate("companySuffix");
        Predicate mathPredicate = new TokenPredicate("mathExpression");

        Predicate geo = PredicateUtils.orPredicate(geoLocal, geoGlobal);
        Predicate geoExact = PredicateUtils.orPredicate(geoLocalExact, geoGlobalExact);

        // Person
        AnalysisRule person = new AnalysisRule();

        Predicate firstAndLastName = PredicateUtils.andPredicate(firstName, lastName);
        Predicate firstOrLast = PredicateUtils.orPredicate(firstName, lastName);
        Predicate firstOrLastAndGeo = PredicateUtils.andPredicate(firstOrLast, geo);

        Predicate[] aa = {exactWiki, companySuffix, keyword, category, PredicateUtils.andPredicate(prioCompanyName, PredicateUtils.notPredicate(firstName))};

        Predicate notWikiNotCompanyPostfix = PredicateUtils.nonePredicate(aa);
        Predicate firstOrLastNotCompany = PredicateUtils.andPredicate(notWikiNotCompanyPostfix, firstOrLastAndGeo);

        Predicate w = PredicateUtils.andPredicate(notWikiNotCompanyPostfix, firstAndLastName);


        Predicate catalogueAndName = PredicateUtils.andPredicate(cataloguePrefix, firstOrLast);
        Predicate whiteBoost = PredicateUtils.anyPredicate(new Predicate[] {firstOrLastNotCompany, w, catalogueAndName});


        person.addPredicateScore(fullName, 10);
        person.addPredicateScore(w, 90);
        person.addPredicateScore(firstOrLastNotCompany, 90);
        person.addPredicateScore(geoExact, -500);
        person.addPredicateScore(phoneNumber, 150);
        person.addPredicateScore(catalogueAndName, 150);
        Predicate[] personOtherPrefixes = {picturePrefix, weatherPrefix, newsPrefix, wikipediaPrefix, tvPrefix};
        person.addPredicateScore(PredicateUtils.anyPredicate(personOtherPrefixes), -500);
        person.addPredicateScore(tns, -500);
        person.addPredicateScore(exactFirstOrLast, -500);

        rules.put("whitePages", person);

        // Company
        AnalysisRule company = new AnalysisRule();

        Predicate categoryOrKeyword = PredicateUtils.orPredicate(category, keyword);
        company.addPredicateScore(categoryOrKeyword, 100);

        Predicate g[] = {companySuffix, keyword, category, prioCompanyName};

        company.addPredicateScore(PredicateUtils.andPredicate(exactWiki, PredicateUtils.nonePredicate(g)), -500);
        Predicate companyNotPerson = PredicateUtils.andPredicate(PredicateUtils.notPredicate(firstAndLastName), companyName);
        company.addPredicateScore(companyNotPerson, 200);
        company.addPredicateScore(PredicateUtils.andPredicate(firstAndLastName, companySuffix), 200);
        company.addPredicateScore(prioCompanyName, 30);
        company.addPredicateScore(geoExact, -500);
        company.addPredicateScore(orgNr, 120);
        company.addPredicateScore(phoneNumber, 200);
        company.addPredicateScore(PredicateUtils.allPredicate(new Predicate[] {PredicateUtils.notPredicate(prioCompanyName), PredicateUtils.notPredicate(companySuffix), firstOrLastAndGeo, PredicateUtils.notPredicate(categoryOrKeyword)}), -500);
        company.addPredicateScore(PredicateUtils.allPredicate(new Predicate[] {cataloguePrefix, firstOrLast, PredicateUtils.notPredicate(companySuffix)}), -500);
        company.addPredicateScore(newsPrefix, -500);
        company.addPredicateScore(tns, -500);
        company.addPredicateScore(exactFirst, -500);

        rules.put("yellowPages", company);

        AnalysisRule globalEnrichment = new AnalysisRule();

        Predicate[] ppp = {keyword, category, firstName,lastName,companyName};

        globalEnrichment.addPredicateScore(PredicateUtils.andPredicate(englishWords, PredicateUtils.notPredicate(wikipedia)), 90);
        globalEnrichment.addPredicateScore(PredicateUtils.anyPredicate(ppp), -500);
        rules.put("globalEnrichment", globalEnrichment);

        AnalysisRule picSearch = new AnalysisRule();

        Predicate[] picOtherPrefixes = {exactCompanyName, cataloguePrefix, weatherPrefix, picturePrefix, newsPrefix, wikipediaPrefix};
        Predicate picNotOtherPrefixes = PredicateUtils.nonePredicate(picOtherPrefixes);

        picSearch.addPredicateScore(PredicateUtils.andPredicate(picNotOtherPrefixes, exactWiki), 550);
        picSearch.addPredicateScore(picturePrefix, 550);
        picSearch.addPredicateScore(geoExact, -600);

        rules.put("picSearch", picSearch);

        AnalysisRule wwikipedia = new AnalysisRule();

        Predicate wikiAndCompany = PredicateUtils.andPredicate(prioCompanyName, wikipedia);

        Predicate[] wikiOtherPrefixes = {cataloguePrefix, weatherPrefix, picturePrefix, newsPrefix};

        Predicate notOtherPrefixes = PredicateUtils.nonePredicate(wikiOtherPrefixes);

        wwikipedia.addPredicateScore(PredicateUtils.andPredicate(wikipediaPrefix, wikipedia), 400);
        wwikipedia.addPredicateScore(PredicateUtils.andPredicate(wikiAndCompany, notOtherPrefixes), -500);
        wwikipedia.addPredicateScore(exactWiki, 300);
        wwikipedia.addPredicateScore(firstAndLastName, -220);
        wwikipedia.addPredicateScore(geoExact, -220);
        wwikipedia.addPredicateScore(tns, -220);

        rules.put("wikipedia", wwikipedia);

        Predicate[] lots = {tns, exactWiki, geo, keyword, category, firstOrLast};

        AnalysisRule news = new AnalysisRule();

        Predicate[] pp = {firstAndLastName, exactWiki, PredicateUtils.notPredicate(companySuffix)}; // and not big sites.

        news.addPredicateScore(PredicateUtils.andPredicate(prioCompanyName, PredicateUtils.neitherPredicate(firstName, geo)), 400);
        news.addPredicateScore(PredicateUtils.allPredicate(pp), 300);
        news.addPredicateScore(newsPrefix, 400);
        news.addPredicateScore(PredicateUtils.truePredicate(), 100);
        news.addPredicateScore(PredicateUtils.andPredicate(PredicateUtils.notPredicate(newsPrefix), categoryOrKeyword), -550);
        news.addPredicateScore(PredicateUtils.andPredicate(companyName, PredicateUtils.notPredicate(prioCompanyName)), -20);
        news.addPredicateScore(PredicateUtils.orPredicate(geoExact, whiteBoost), -20);
        news.addPredicateScore(PredicateUtils.orPredicate(tns, companyNotPerson), -20);
        news.addPredicateScore(cataloguePrefix, -500);
        news.addPredicateScore(PredicateUtils.allPredicate(new Predicate[] {PredicateUtils.notPredicate(firstAndLastName), exactWiki}), -20);
        news.addPredicateScore(wikipediaPrefix, -500);
        news.addPredicateScore(weatherPrefix, -500);
        news.addPredicateScore(PredicateUtils.andPredicate(englishWords, PredicateUtils.nonePredicate(lots)), -500);

        news.addPredicateScore(PredicateUtils.andPredicate(PredicateUtils.neitherPredicate(companyName, exactWiki), exactFirst), -500);

        rules.put("news", news);


        AnalysisRule tv = new AnalysisRule();

        Predicate[] otherPrefTv = {cataloguePrefix, wikipediaPrefix, picturePrefix, weatherPrefix};

        tv.addPredicateScore(PredicateUtils.anyPredicate(otherPrefTv), -500);
        tv.addPredicateScore(tvPrefix, 1000);
        tv.addPredicateScore(PredicateUtils.notPredicate(geo), 500);
        tv.addPredicateScore(newsPrefix, -420);
        tv.addPredicateScore(categoryOrKeyword, -420);
        tv.addPredicateScore(tns, -500);
        rules.put("tv", tv);

        // Global search
        AnalysisRule globalSearch = new AnalysisRule();

        globalSearch.addPredicateScore(PredicateUtils.andPredicate(englishWords, PredicateUtils.nonePredicate(lots)), 100);
        rules.put("globalSearch", globalSearch);


        AnalysisRule weather = new AnalysisRule();
        Predicate[] allCompanyAndPresonHits = {fullName, keyword, category, companyName};
        weather.addPredicateScore(PredicateUtils.andPredicate(weatherPrefix, geo), 400);
        weather.addPredicateScore(geoExact, 500);
        weather.addPredicateScore(PredicateUtils.andPredicate(PredicateUtils.notPredicate(geo), PredicateUtils.anyPredicate(allCompanyAndPresonHits)), -500);
        weather.addPredicateScore(tns, -500);
        rules.put("weather", weather);

        AnalysisRule mathExpression = new AnalysisRule();
        mathExpression.addPredicateScore(mathPredicate, 500);
        rules.put("mathExpression", mathExpression);

    }

    public Map getRules() {
        return rules;
    }


    public AnalysisRule getRule(String ruleName) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: getRule()" + ruleName);
        }
        AnalysisRule rule= (AnalysisRule) rules.get(ruleName);

        return rule;
    }
}
