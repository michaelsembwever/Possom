/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.fast.searchengine.test;

import no.fast.ds.search.IFastSearchEngineFactory;
import no.fast.ds.search.IFastSearchEngine;
import no.schibstedsok.searchportal.fast.searchengine.test.MockupFastSearchEngine;

import java.net.MalformedURLException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MockupFastSearchEngineFactory implements IFastSearchEngineFactory {
    public IFastSearchEngine createSearchEngine(String s) throws MalformedURLException {
        return new MockupFastSearchEngine(s);
    }
}
