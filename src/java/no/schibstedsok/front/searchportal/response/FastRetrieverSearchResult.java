/*
 * Copyright (2005) Schibsted S¿k AS
 * 
 */
package no.schibstedsok.front.searchportal.response;

import no.fast.ds.search.IDocumentSummary;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastRetrieverSearchResult extends FastSearchResult {

    public FastRetrieverSearchResult(IDocumentSummary summary) {
        super(summary);
    }

    protected void populateFields(IDocumentSummary summary) {
        setSummary(getSummaryField(summary, "newsbody"));
        setTitle(getSummaryField(summary, "newstitle"));
        setUrl(getSummaryField(summary, "url"));
        setClickUrl(getSummaryField(summary, "url"));
        setNewsSource(getSummaryField(summary, "newssource"));
        setDocDateTime(getSummaryField(summary, "docdatetime"));
    }
}
