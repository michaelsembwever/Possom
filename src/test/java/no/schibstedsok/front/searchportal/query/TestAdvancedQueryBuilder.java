package no.schibstedsok.front.searchportal.query;

import no.schibstedsok.front.searchportal.TestCase;

/**
 * TestAdvancedQueryBuilder is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class TestAdvancedQueryBuilder extends TestCase {


    public void testQuery(){


        String qAll="ola marius";
        String qAny = "hoff sagli";
        String qPhrase = null;
        String qNot = null;



        AdvancedQueryBuilder qb =
                new AdvancedQueryBuilder(qAll,
                                         qPhrase,
                                         qAny,
                                         qNot);

        assertEquals("ola AND marius OR hoff OR sagli",
                qb.getQuery());
        qb = new AdvancedQueryBuilder();

        qb.setQueryNot("ola marius hoff sagli");

        assertEquals("NOT ola ANDNOT marius ANDNOT hoff ANDNOT sagli",
                qb.getQuery());
        qb.setQueryPhrase("dette er en test");
        assertEquals("NOT ola ANDNOT marius ANDNOT hoff ANDNOT sagli AND \"dette er en test\"",
                qb.getQuery());

    }

    public void testIsAdvanced (){
        assertTrue(AdvancedQueryBuilder.isAdvancedQuery("ola AND marius NOT sagli"));
        assertFalse(AdvancedQueryBuilder.isAdvancedQuery("ola marius hoff sagli"));
    }


    public void testRemoveDups(){
        assertEquals("marius", AdvancedQueryBuilder.trimDuplicateSpaces("           marius").trim());
        assertEquals("\" marius\"", AdvancedQueryBuilder.trimDuplicateSpaces("\"           marius\"").trim());

    }
}
