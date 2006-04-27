// Copyright (2006) Schibsted Søk AS
/*
 * WebSearchCommand.java
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Map;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;

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
    public WebSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    

    /**
     *
     * @param clause The clause to examine.
     */
    protected void visitImpl(final XorClause clause) {
        if (clause.getHint() == XorClause.PHRASE_ON_LEFT) {
            // Web searches should use phrases over separate words.
            clause.getFirstClause().accept(this);
        } else {
            // All other high level clauses are ignored.
            clause.getSecondClause().accept(this);
        }
    }


    /**
     * LeafClause
     *
     * A leaf clause with a site field does not add anything to the query.
     *
     */
    protected void visitImpl(final LeafClause clause) {
        if (!hasSiteField(clause)) {
            super.visitImpl(clause);
        }
    }

    /**
     * PhraseClause
     *
     * A phrase with a site field does not add anything to the query.
     *
     */
    protected void visitImpl(final PhraseClause clause) {
        if (!hasSiteField(clause)) {
            super.visitImpl(clause);
        }
    }

}
