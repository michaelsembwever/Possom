/*
 * BlogSearchCommand.java
 *
 * Created on July 11, 2006, 12:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.Map;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResult;

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
public class BlogSearchCommand extends AbstractESPFastSearchCommand {

    /** Creates a new instance of FastSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public BlogSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    // Public --------------------------------------------------------
    public SearchResult execute() {

        if (getSearchConfiguration().getName().equals("blogCounter")) {
            final BasicSearchResult r = new BasicSearchResult(this);
            r.setHitCount(123456789);

            return r;
        }

        return super.execute();
    }

    protected void visitImpl(final LeafClause clause) {

        if (clause.getField() == null 
                && !getTransformedTerm(clause).trim().equals("")) 
        {
            appendToQueryRepresentation("(");
            appendToQueryRepresentation("content:");
            appendToQueryRepresentation(getTransformedTerm(clause));
            appendToQueryRepresentation(" or ");
            appendToQueryRepresentation("extended:");
            appendToQueryRepresentation(getTransformedTerm(clause));
            appendToQueryRepresentation(")");
        }
    }
}
