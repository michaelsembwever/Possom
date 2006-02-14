/*
 * OlympicTokenEvaluator.java
 *
 * Created on February 8, 2006, 5:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.analyzer;

import java.util.Locale;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluator;
import no.schibstedsok.front.searchportal.util.OlympicData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author magnuse
 */
public class OlympicTokenEvaluator implements TokenEvaluator {

    private static Locale  locale = new Locale("no", "NO");
    
    private static Log log = LogFactory.getLog(OlympicTokenEvaluator.class);
    /** Creates a new instance of OlympicTokenEvaluator */
    public OlympicTokenEvaluator() {
    }
    
    public boolean evaluateToken(String token, String term, String query) {
        
        OlympicData data = OlympicData.instance();
        
        if ("olympicParticipant".equals(token)) {
            return data.getParticipants().containsKey(query.toLowerCase(locale));
        }
        
        if ("olympicDicipline".equals(token)) {
            return data.getDiciplines().containsKey(query.toLowerCase(locale));
        }

        if ("olympicTerm".equals(token)) {
            return data.getTerms().containsKey(query.toLowerCase(locale));
        }

        return false;
    }

    public boolean isQueryDependant() {
         return false;
    }
}
