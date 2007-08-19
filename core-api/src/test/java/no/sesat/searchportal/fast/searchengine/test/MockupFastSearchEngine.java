/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.searchportal.fast.searchengine.test;

import no.fast.ds.search.*;
import no.sesat.searchportal.result.test.MockupQueryResult;

import java.util.List;
import java.io.IOException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class MockupFastSearchEngine implements IFastSearchEngine {

    private String url;

    public MockupFastSearchEngine(String  url) {
        this.url = url;
    }

    public List getCollections() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCollection() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCluster() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IQueryResult search(String string) throws IOException, SearchEngineException {
        return new MockupQueryResult();
    }

    public IQueryResult search(IQuery iQuery) throws IOException, SearchEngineException {

        MockupQueryResult result = new MockupQueryResult();


        try {
            ISearchParameter navigators = iQuery.getParameter(BaseParameter.NAVIGATORS);

            String navs[] = navigators.getStringValue().split(",");

            for (int i = 0; i < navs.length; i++) {
                String nav = navs[i];
                INavigator navigator = new MockupNavigator(nav);
                result.addNavigator(navigator);
            }

        } catch (NoSuchParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }
}
