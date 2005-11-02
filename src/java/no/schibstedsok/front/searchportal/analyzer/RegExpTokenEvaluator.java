package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import no.schibstedsok.front.searchportal.query.StopWordRemover;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class RegExpTokenEvaluator implements TokenEvaluator, StopWordRemover {

    Log log = LogFactory.getLog(RegExpTokenEvaluator.class);
    private List expressions;

    public RegExpTokenEvaluator(List expressions) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: RegExpTokenEvaluator()");
        }
        this.expressions = expressions;
    }

    public boolean evaluateToken(String token, String query) {

        if(log.isDebugEnabled()){
            log.debug("ENTR: evaluateToken()");
        }
        for (Iterator iterator = expressions.iterator(); iterator.hasNext();) {
            Pattern p = (Pattern) iterator.next();

            log.debug(query);

            Matcher m = p.matcher(query);

            if (m.find()) {
                return true;
            }
        }
        return false;
    }

    public String removeStopWords(String originalQuery) {

        if (originalQuery.indexOf('"') > -1) {
            return originalQuery;
        }

        for (Iterator iterator = expressions.iterator(); iterator.hasNext();) {
            Pattern p = (Pattern) iterator.next();
            Matcher m = p.matcher(originalQuery);
            originalQuery = m.replaceAll("");
        }

        return originalQuery;
    }
}
