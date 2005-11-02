package no.schibstedsok.front.searchportal.analyzer;

import no.schibstedsok.front.searchportal.query.StopWordRemover;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class
        RegExpEvaluators {
    static Log log = LogFactory.getLog(RegExpEvaluators.class);
    static Map expressions = new HashMap();
    static Map compiledExpressions = new HashMap();

    static {
        // Replace w/ xml file.
        List cataloguePrefix = new ArrayList();
        cataloguePrefix.add("telefon(nummer){0,1} (til|for){0,1}");
        cataloguePrefix.add("tlf (til|for){0,1}");
        cataloguePrefix.add("nummer (til|for){0,1}");
        cataloguePrefix.add("adresse(n){0,1} (til|for){0,1}");
        cataloguePrefix.add("Hvor (er){0,1}");
        expressions.put("cataloguePrefix", cataloguePrefix);

        List phoneNumber = new ArrayList();
        phoneNumber.add("((\\+|00)47){0,1}\\s*(\\d\\s{0,1}){8}");
        expressions.put("phoneNumber", phoneNumber);

        List orgNr = new ArrayList();
        orgNr.add("\\d{9}");
        expressions.put("orgNr", orgNr);

        List picturePrefix = new ArrayList();
        picturePrefix.add("^bilde(r){0,1}\\s{0,1}(av){0,1}");
        picturePrefix.add("^jpg ");
        expressions.put("picturePrefix", picturePrefix);

        List newsPrefix = new ArrayList();
        newsPrefix.add("^nyhet(er){0,1}\\s{0,1}(om){0,1}");
        newsPrefix.add("^(siste ){0,1}nytt\\s{0,1}(om){0,1}");
        newsPrefix.add("^aviser");
        newsPrefix.add("^nettaviser");
        expressions.put("newsPrefix", newsPrefix);

        List wikiPrefix = new ArrayList();
        wikiPrefix.add("wiki(pedia){0,1} ");
        wikiPrefix.add("beskriv ");
        wikiPrefix.add("leksikon ");
        wikiPrefix.add("fakta ");
        expressions.put("wikipediaPrefix", wikiPrefix);

        List tvPrefix = new ArrayList();
        tvPrefix.add("^p.* tv (i\\s{0,1}dag){0,1}");
        tvPrefix.add("^programoversikt ");
        tvPrefix.add("^program ");
        tvPrefix.add("^tv(-| )program ");
        tvPrefix.add("^tv ");
        tvPrefix.add("^fjernsyn");
        expressions.put("tvPrefix", tvPrefix);

        List asPrefix = new ArrayList();
        asPrefix.add("\\sas\\s*");
        asPrefix.add("\\sasa\\s*");
        asPrefix.add("\\s& co\\s*");
        expressions.put("companySuffix",asPrefix);


        List weatherPrefix=new ArrayList();
        weatherPrefix.add("^regn ");
        weatherPrefix.add("^v.*r(et|melding|varsel){0,1}\\s{0,1}(i|p.*|for){0,1} ");
        weatherPrefix.add("^temperatur\\s{0,1}(i|p.*|for){0,1} ");
        weatherPrefix.add("^varsel\\s{0,1}(i|p.*|for){0,1} ");
        expressions.put("weatherPrefix", weatherPrefix);

        List mathExpression = new ArrayList();
        mathExpression.add("[\\+\\-\\*\\/(]");
        expressions.put("mathExpression", mathExpression);

        for (Iterator iterator = expressions.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();

            List uncompiled = (List) expressions.get(name);
            List compiled = new ArrayList();

            for (Iterator iterator1 = uncompiled.iterator(); iterator1.hasNext();) {
                String expression = (String) iterator1.next();
                if (log.isDebugEnabled()) {
                    log.debug("Compiling expression " + expression);
                }

                Pattern p = Pattern.compile("\\s*" + expression + "\\s*", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
                compiled.add(p);

            }

            compiledExpressions.put(name, compiled);
        }
    }

    public static TokenEvaluator getEvaluator(String token) {
        return new RegExpTokenEvaluator((List) compiledExpressions.get(token));
    }

    public static StopWordRemover getStopWordRemover(String token) {
        return (StopWordRemover) getEvaluator(token);
    }
}
