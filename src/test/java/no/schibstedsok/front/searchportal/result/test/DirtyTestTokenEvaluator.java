package no.schibstedsok.front.searchportal.result.test;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.analyzer.VeryFastTokenEvaluator;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluator;

/**
 * DirtyTestTokenEvaluator is part of no.schibstedsok.front.searchportal.result.test
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class DirtyTestTokenEvaluator extends TestCase {



    public void testEvaluator (){
        System.out.println(" --- 1 ---");
        TokenEvaluator te = new VeryFastTokenEvaluator(null, "ola marius hoff sagli");
        System.out.println("firstname ? "+te.evaluateToken(VeryFastTokenEvaluator.FIRSTNAME, null));
        System.out.println("lastname ? "+te.evaluateToken(VeryFastTokenEvaluator.LASTNAME, null));
        System.out.println("company ? "+te.evaluateToken(VeryFastTokenEvaluator.COMPANY, null));
        System.out.println("category ? "+te.evaluateToken(VeryFastTokenEvaluator.CATEGORY, null));
        System.out.println("geo ? "+te.evaluateToken(VeryFastTokenEvaluator.GEO, null));
        System.out.println("keywords ? "+te.evaluateToken(VeryFastTokenEvaluator.KEYWORDS, null));


        System.out.println(" --- 2 ---");
        TokenEvaluator te2 = new VeryFastTokenEvaluator(null, "aftenposten");
        System.out.println("firstname ? "+te2.evaluateToken(VeryFastTokenEvaluator.FIRSTNAME, null));
        System.out.println("lastname ? "+te2.evaluateToken(VeryFastTokenEvaluator.LASTNAME, null));
        System.out.println("company ? "+te2.evaluateToken(VeryFastTokenEvaluator.COMPANY, null));
        System.out.println("category ? "+te2.evaluateToken(VeryFastTokenEvaluator.CATEGORY, null));
        System.out.println("geo ? "+te2.evaluateToken(VeryFastTokenEvaluator.GEO, null));
        System.out.println("keywords ? "+te2.evaluateToken(VeryFastTokenEvaluator.KEYWORDS, null));

        System.out.println(" --- 3 ---");
        TokenEvaluator te3 = new VeryFastTokenEvaluator(null, "ola marius schibsted");
        System.out.println("firstname ? "+te3.evaluateToken(VeryFastTokenEvaluator.FIRSTNAME, null));
        System.out.println("lastname ? "+te3.evaluateToken(VeryFastTokenEvaluator.LASTNAME, null));
        System.out.println("company ? "+te3.evaluateToken(VeryFastTokenEvaluator.COMPANY, null));
        System.out.println("category ? "+te3.evaluateToken(VeryFastTokenEvaluator.CATEGORY, null));
        System.out.println("geo ? "+te3.evaluateToken(VeryFastTokenEvaluator.GEO, null));
        System.out.println("keywords ? "+te3.evaluateToken(VeryFastTokenEvaluator.KEYWORDS, null));

    }
}
