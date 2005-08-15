/*
 * Copyright (2005) Schibsted S¿k AS
 * 
 */
package no.schibstedsok.front.searchportal.response;

import no.fast.ds.search.IDocumentSummary;

/**
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision$</tt>
 */
public class FastWikiSearchResult extends FastSearchResult {

    public FastWikiSearchResult(IDocumentSummary summary) {
        super(summary);
    }

    protected void populateFields(IDocumentSummary summary) {
        setSummary(getSummaryField(summary, "wikibody"));
        setTitle(getSummaryField(summary, "title"));
        setUrl(getSummaryField(summary, "url"));
        setClickUrl(getSummaryField(summary, "url"));
    }
}
