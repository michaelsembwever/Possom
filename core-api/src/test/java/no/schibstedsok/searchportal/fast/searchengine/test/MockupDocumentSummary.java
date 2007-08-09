/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.fast.searchengine.test;

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
