package no.schibstedsok.searchportal.fast.searchengine.test;

import no.fast.ds.search.*;
import no.schibstedsok.searchportal.result.test.MockupQueryResult;

import java.util.List;
import java.io.IOException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
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
