/* Copyright (2008) Schibsted Søk AS
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
package no.sesat.search.mode.command;

import java.util.Collections;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.config.SearchTab.EnrichmentHint;
import no.sesat.search.view.config.SearchTab.EnrichmentPlacementHint;
import no.sesat.search.view.config.SearchTab.Layout;
import no.sesat.search.view.config.SearchTab.Scope;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 *
 * @version $Id$
 */
public class MathExpressionSearchCommandTest extends AbstractSearchCommandTest {

    @Test
    public void testPlus()  throws Exception{
        executeTestOfQuery("1+1", "1+1 = 2");
        executeTestOfQuery("1 + 1", "1 + 1 = 2");
    }

    @Test
    public void testMinus()  throws Exception{
        executeTestOfQuery("2-1", "2-1 = 1");
        executeTestOfQuery("2 - 1", "2 - 1 = 1");
    }

    @Test
    public void testMultiply()  throws Exception{
        executeTestOfQuery("3*4", "3*4 = 12");
        executeTestOfQuery("3 * 4", "3 * 4 = 12");
    }

    @Test
    public void testDivide()  throws Exception{
        executeTestOfQuery("20/4", "20/4 = 5");
        executeTestOfQuery("20 / 4", "20 / 4 = 5");
    }

    /** Examples taken from JEP homepage
     * http://www.singularsys.com/jep/exampleapplets.html
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testComplex()  throws Exception{
        executeTestOfQuery("80 * e^(-((i/40)^2))", "80 * e^(-((i/40)^2)) = 80.05");
        executeTestOfQuery("20*ln(i/10)", "20*ln(i/10) = -46.052 + 31.416i");
        executeTestOfQuery("10*tan(i/30)", "10*tan(i/30) = -0.333");
        executeTestOfQuery("50 * sin(i/20)", "50 * sin(i/20) = 0 + 2.501i");
    }


    /**
     * Asserts that generated query equals the expected generated query.
     *
     * @param query         The query.
     * @param wantedQuery   The expected query.
     */
    private void executeTestOfQuery(
            final String query,
            final String wantedQuery)  throws SiteKeyedFactoryInstantiationException{

        final SearchTab fakeTab = new SearchTab(null, "fake-view", "default-mode", "test", null, null, true,
                null, Collections.<EnrichmentPlacementHint>emptyList(), Collections.<EnrichmentHint>emptyList(), null,
                0, 0, Collections.<String>emptyList(), Collections.<String>emptyList(), false, false,
                null, Collections.<String,Layout>emptyMap(), Scope.REQUEST);

        final SearchCommand.Context cxt = createCommandContext(query, fakeTab, "default-math-command");
        final AbstractSearchCommand cmd = new MathExpressionSearchCommand(cxt);
        final ResultItem result = cmd.execute().getResults().get(0);

        assertEquals("Generated result does not match wanted query", wantedQuery, result.getField("result"));
    }
}
