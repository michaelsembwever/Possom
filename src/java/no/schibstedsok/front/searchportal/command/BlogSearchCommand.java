/*
 * BlogSearchCommand.java
 *
 * Created on July 11, 2006, 12:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Map;
import no.schibstedsok.front.searchportal.query.LeafClause;

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
public class BlogSearchCommand extends AdvancedFastSearchCommand {

    /** Creates a new instance of FastSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public BlogSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
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
