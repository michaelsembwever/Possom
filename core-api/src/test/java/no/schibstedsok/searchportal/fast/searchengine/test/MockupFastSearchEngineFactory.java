// Copyright (2007) Schibsted Søk AS
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
