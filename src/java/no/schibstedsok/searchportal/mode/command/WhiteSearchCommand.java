// Copyright (2006) Schibsted Søk AS
/*
 * WhiteSearchCommand.java
 *
 * Created on March 4, 2006, 1:59 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.Map;
import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchType;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.IntegerClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhoneNumberClause;
import no.schibstedsok.searchportal.query.XorClause;

/**
 *
 * @author magnuse
 */
public class WhiteSearchCommand extends CorrectingFastSearchCommand {

    private static final String PREFIX_INTEGER="whitepages:";
    private static final String PREFIX_PHONETIC="whitephon:";

    /**
     *
     * @param cxt
     * @param parameters
     */
    public WhiteSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    /**
     * Adds non phonetic prefix to integer terms.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final IntegerClause clause) {
        if (! getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER);
        }

        super.visitImpl(clause);
    }

    /**
     * Adds non phonetic prefix to phone number terms.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final PhoneNumberClause clause) {
        if (! getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER);
        }
        super.visitImpl(clause);
    }
    /**
     * Adds phonetic prefix to a leaf clause.
     * Remove dots from words. (people, street, suburb, or city names do not have dots.)
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
            if (! getTransformedTerm(clause).equals("")) {
                appendToQueryRepresentation(PREFIX_PHONETIC);
            }

            appendToQueryRepresentation(getTransformedTerm(clause).replaceAll("\\.",""));
        }
    }

    /**
     *
     * An implementation that ignores phrase searches.
     *
     * Visits only the left clause, unless that clause is a phrase or organisation clause, in
     * which case only the right clause is visited. Phrase and organisation searches are not
     * possible against the white index.
     *
     */
    protected void visitImpl(final XorClause clause) {

       if (clause.getHint() == XorClause.PHRASE_ON_LEFT || clause.getHint() == XorClause.NUMBER_GROUP_ON_LEFT) {
           clause.getSecondClause().accept(this);
       } else {
           clause.getFirstClause().accept(this);
       }
    }
    
    private boolean isEmptyLeaf(final Clause clause) {
        if (clause instanceof LeafClause) {
            final LeafClause leaf = (LeafClause) clause;
            return leaf.getField() != null;
        }

        return false;
    }

}
