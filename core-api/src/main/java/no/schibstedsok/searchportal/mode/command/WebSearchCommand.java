// Copyright (2006-2007) Schibsted Søk AS
/*
 * WebSearchCommand.java
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;

/**
 *
 * A search command for the web search.
 * @author magnuse
 */
public class WebSearchCommand extends FastSearchCommand {

    /** Creates a new instance of WebSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public WebSearchCommand(
            final Context cxt,
            final DataModel datamodel) {

        super(cxt, datamodel);
    }



    /**
     *
     * @param clause The clause to examine.
     */
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {
        switch(clause.getHint()){
            case PHRASE_ON_LEFT:
                // Web searches should use phrases over separate words.
                clause.getFirstClause().accept(visitor);
                break;
            default:
                // All other high level clauses are ignored.
                clause.getSecondClause().accept(visitor);
                break;
        }
    }


}
