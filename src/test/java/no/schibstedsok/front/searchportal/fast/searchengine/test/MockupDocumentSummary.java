package no.schibstedsok.front.searchportal.fast.searchengine.test;

import no.fast.ds.search.IDocumentSummary;
import no.fast.ds.search.IDocumentSummaryField;

import java.util.Iterator;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MockupDocumentSummary implements IDocumentSummary {

    int resultNo;

    public MockupDocumentSummary(int i) {
        resultNo = i;
    }

    public int getDocNo() {
        return 0;
    }

    public int fieldCount() {
        return 0;
    }

    public IDocumentSummaryField getSummaryField(int i) {
        return null;
    }

    public IDocumentSummaryField getSummaryField(String string) {
        return new MockupDocumentSummaryField(string, "result number " + resultNo);
    }

    public Iterator summaryFields() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
