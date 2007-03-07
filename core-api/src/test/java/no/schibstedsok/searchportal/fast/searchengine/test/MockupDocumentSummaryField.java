// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.fast.searchengine.test;

import no.fast.ds.search.IDocumentSummaryField;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MockupDocumentSummaryField implements IDocumentSummaryField {
    private String name;
    private String summary;

    public MockupDocumentSummaryField(String name, String summary) {
        this.name = name;
        this.summary = summary;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public int getType() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
