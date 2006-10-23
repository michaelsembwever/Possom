package no.schibstedsok.searchportal.result.test;

import no.fast.ds.search.*;
import no.schibstedsok.searchportal.fast.searchengine.test.MockupDocumentSummary;

import java.util.ListIterator;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MockupQueryResult implements IQueryResult {
    private Map navigators = new HashMap();

    public int getDocCount() {
        return 1000;
    }

    public long getMaxRank() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getTimeUsed() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IQuery getOriginalQuery() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IDocumentSummary getDocument(int i) throws IndexOutOfBoundsException {
        return new MockupDocumentSummary(i);
    }

    public ListIterator documents() throws SearchEngineException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IQueryTransformations getQueryTransformations(boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasBeenResubmitted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator clusterNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator clusters() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ICluster getCluster(String string) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ICluster getCluster() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator navigatorNames() {
        return navigators.keySet().iterator();
    }

    public Iterator navigators() {
        return navigators.values().iterator();
    }

    public int navigatorCount() {
        return navigators.size();
    }

    public INavigator getNavigator(String string) {
        return (INavigator) navigators.get(string);
    }

    public void addNavigator(INavigator navigator) {
        navigators.put(navigator.getName(), navigator);
    }
}
