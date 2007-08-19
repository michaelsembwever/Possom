/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.searchportal.fast.searchengine.test;

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
