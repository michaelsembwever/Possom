// Copyright (2007) Schibsted Søk AS
/*
 * BlogSearchCommand.java
 *
 * Created on July 11, 2006, 12:34 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.query.UrlClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 *
 * Implementation of blog search command.
 *
 * Due to how the blog index is configured queries sent to the index must
 * be applied to two composite fields:
 *
 * A search for "sesam bloggsøk" results in the following query sent to the index:
 *
 * (content:sesam or extended:sesam) and (content:bloggsøk or extended:bloggsøk)
 *
 * @author maek
 */
public final class BlogSearchCommand extends AbstractESPFastSearchCommand {

    /** Creates a new instance of FastSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public BlogSearchCommand(
            final Context cxt,
            final DataModel datamodel) {

        super(cxt, datamodel);
    }

    // Public --------------------------------------------------------
    public SearchResult execute() {
        final SearchResult result = super.execute();

        if (getParameter("collapse") != null && !getParameter("collapse").equals("")) {
            final SearchResultItem item = result.getResults().get(0);

            final String publishedTimed = item.getField("publishedtime");

            if (! publishedTimed.startsWith("1970")) {
                result.addField("expandedBlog", item.getField("title"));
            }
        }

        return result;
    }


    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null
                && !getTransformedTerm(clause).trim().equals("")) {
            appendTermRepresentation(getTransformedTerm(clause));
        } else if (getFieldFilter(clause) == null) {
            appendTermRepresentation(escapeFieldedLeaf(clause));
        }
    }

    /**
     * Adds quotes around the URL. Failing so will produce syntax error in filter.
     *
     * @param clause The url clause.
     */
    protected void visitImpl(final UrlClause clause) {
        appendTermRepresentation('"' + getTransformedTerm(clause) + '"');
    }

    private void appendTermRepresentation(final String term) {
        appendToQueryRepresentation("(");
        appendToQueryRepresentation("content:");
        appendToQueryRepresentation(term);
        appendToQueryRepresentation(" or ");
        appendToQueryRepresentation("extended:");
        appendToQueryRepresentation(term);
        appendToQueryRepresentation(")");
    }

}
