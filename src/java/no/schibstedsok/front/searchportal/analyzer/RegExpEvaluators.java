// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.analyzer;

import no.schibstedsok.front.searchportal.query.StopWordRemover;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class RegExpEvaluators {

    private static final Log LOG = LogFactory.getLog(RegExpEvaluators.class);

    private static Map/*<TokenPredicate>,<Collection>*/ expressions = new HashMap/*<TokenPredicate>,<Collection>*/();
    private static Map/*<TokenPredicate>,<Collection>*/ expressionsQueryDependant = new HashMap/*<TokenPredicate>,<Collection>*/();
    private static Map/*<TokenPredicate>,<Collection>*/ regExpEvaluators = new HashMap/*<TokenPredicate>,<Collection>*/();

    static {
        // [TODO] Replace w/ xml file.
        final Collection cataloguePrefix = new ArrayList();
        cataloguePrefix.add("telefon(nummer){0,1} (til|for){0,1}");
        cataloguePrefix.add("tlf (til|for){0,1}");
        cataloguePrefix.add("nummer (til|for){0,1}");
        cataloguePrefix.add("adresse(n){0,1} (til|for){0,1}");
        cataloguePrefix.add("Hvor (er){0,1}");
        expressions.put(TokenPredicate.CATALOGUEPREFIX, cataloguePrefix);
        expressionsQueryDependant.put(TokenPredicate.CATALOGUEPREFIX, Boolean.FALSE);

        final Collection phoneNumber = new ArrayList();
        phoneNumber.add("((\\+|00)47){0,1}\\s*(\\d\\s{0,1}){8}");
        expressions.put(TokenPredicate.PHONENUMBER, phoneNumber);
        expressionsQueryDependant.put(TokenPredicate.PHONENUMBER, Boolean.FALSE);

        final Collection orgNr = new ArrayList();
        orgNr.add("\\d{9}");
        expressions.put(TokenPredicate.ORGNR, orgNr);
        expressionsQueryDependant.put(TokenPredicate.ORGNR, Boolean.FALSE);

        final Collection picturePrefix = new ArrayList();
        picturePrefix.add("^bilde(r){0,1}\\s{0,1}(av){0,1}");
        picturePrefix.add("^jpg");
        expressions.put(TokenPredicate.PICTUREPREFIX, picturePrefix);
        expressionsQueryDependant.put(TokenPredicate.PICTUREPREFIX, Boolean.TRUE);

        final Collection newsPrefix = new ArrayList();
        newsPrefix.add("^nyhet(er){0,1}\\s{0,1}(om){0,1}");
        newsPrefix.add("^(siste ){0,1}nytt\\s{0,1}(om){0,1}");
        newsPrefix.add("^aviser");
        newsPrefix.add("^nettaviser");
        expressions.put(TokenPredicate.NEWSPREFIX, newsPrefix);
        expressionsQueryDependant.put(TokenPredicate.NEWSPREFIX, Boolean.TRUE);

        final Collection wikiPrefix = new ArrayList();
        wikiPrefix.add("wiki(pedia){0,1} ");
        wikiPrefix.add("beskriv");
        wikiPrefix.add("leksikon");
        wikiPrefix.add("fakta");
        expressions.put(TokenPredicate.WIKIPEDIAPREFIX, wikiPrefix);
        expressionsQueryDependant.put(TokenPredicate.WIKIPEDIAPREFIX, Boolean.TRUE);

        final Collection tvPrefix = new ArrayList();
        tvPrefix.add("^p.* tv (i\\s{0,1}dag){0,1}");
        tvPrefix.add("^programoversikt ");
        tvPrefix.add("^program");
        tvPrefix.add("^tv(-| )program");
        tvPrefix.add("^tv");
        tvPrefix.add("^fjernsyn");
        expressions.put(TokenPredicate.TVPREFIX, tvPrefix);
        expressionsQueryDependant.put(TokenPredicate.TVPREFIX, Boolean.TRUE);

        final Collection asPrefix = new ArrayList();
        asPrefix.add("\\sas\\s*");
        asPrefix.add("\\sasa\\s*");
        asPrefix.add("\\s& co\\s*");
        expressions.put(TokenPredicate.COMPANYSUFFIX, asPrefix);
        expressionsQueryDependant.put(TokenPredicate.COMPANYSUFFIX, Boolean.FALSE);


        final Collection weatherPrefix = new ArrayList();
        weatherPrefix.add("^regn");
        weatherPrefix.add("^v.*r(et|melding|varsel){0,1}\\s{0,1}(i|p.*|for){0,1}");
        weatherPrefix.add("^temperatur\\s{0,1}(i|p.*|for){0,1}");
        weatherPrefix.add("^varsel\\s{0,1}(i|p.*|for){0,1}");
        expressions.put(TokenPredicate.WEATHERPREFIX, weatherPrefix);
        expressionsQueryDependant.put(TokenPredicate.WEATHERPREFIX, Boolean.TRUE);

        final Collection mathExpression = new ArrayList();
        mathExpression.add("[\\+\\-\\*\\/(]");
        expressions.put(TokenPredicate.MATHPREDICATE, mathExpression);
        expressionsQueryDependant.put(TokenPredicate.MATHPREDICATE, Boolean.FALSE);


        for (Iterator iterator = expressions.keySet().iterator(); iterator.hasNext();) {
            final TokenPredicate token = (TokenPredicate) iterator.next();

            final Collection uncompiled = (Collection) expressions.get(token);
            final Collection compiled = new ArrayList();

            for (Iterator iterator1 = uncompiled.iterator(); iterator1.hasNext();) {
                final String expression = (String) iterator1.next();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Compiling expression " + expression);
                }

                final Pattern p = Pattern.compile("\\s*" + expression + "\\s*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                compiled.add(p);

            }
            final RegExpTokenEvaluator evaluator = new RegExpTokenEvaluator(
                    (Collection) compiled,
                    ((Boolean) expressionsQueryDependant.get(token)).booleanValue());

            regExpEvaluators.put(token, evaluator);
        }
    }

    private RegExpEvaluators() {
    }

    /**
     *
     * @param token
     * @return
     */
    public static RegExpTokenEvaluator getEvaluator(final TokenPredicate token) {
        return (RegExpTokenEvaluator) regExpEvaluators.get(token);
    }

    /**
     *
     * @param token
     * @return
     */
    public static StopWordRemover getStopWordRemover(final TokenPredicate token) {
        return (StopWordRemover) getEvaluator(token);
    }
}
