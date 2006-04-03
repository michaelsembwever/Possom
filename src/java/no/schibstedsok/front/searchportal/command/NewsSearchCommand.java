// Copyright (2006) Schibsted Søk AS
/*
 * NewsSearchCommand.java
 *
 * Created on March 7, 2006, 5:31 PM
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Map;
import no.schibstedsok.front.searchportal.command.SearchCommand.Context;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;

/**
 *
 * @author magnuse
 */
public class NewsSearchCommand extends FastSearchCommand {
    
    private static final String FAST_SOURCE_FILTER_FIELD = "newssource";
    private static final String SESAM_SOURCE_FIELD = "nyhetskilde";
    
    // Filter used to get all articles.
    private static final String FAST_SIZE_HACK = " +size:>0";
    
    /** Creates a new instance of NewsSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public NewsSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }
    
    private StringBuilder filterBuilder = null;
    
    /**
     *
     * @param clause The clause to examine.
     */
    protected void visitImpl(final XorClause clause) {
        if (clause.getHint() == XorClause.PHRASE_ON_LEFT) {
            // News searches should use phrases over separate words.
            clause.getFirstClause().accept(this);
        } else {
            // All other high level clauses are ignored.
            clause.getSecondClause().accept(this);
        }
    }
    
    /**
     * LeafClause
     *
     * A leaf clause with a site field does not add anything to the query. Also
     * if the query just contains the prefix do not output anything.
     *
     */
    protected void visitImpl(final LeafClause clause) {
        if (! (hasSourceField(clause) || containsJustThePrefix())) {
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
        if (!hasSourceField(clause)) {
            super.visitImpl(clause);
        }
    }
    
    protected String getAdditionalFilter() {
        synchronized (this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuilder();
                new FilterVisitor().visit(context.getQuery().getRootClause());
            }
            
            // Add filter to retrieve all documents.
            if (containsJustThePrefix()) {
                filterBuilder.append(FAST_SIZE_HACK);
            }
            
            return filterBuilder.toString();
        }
    }
    
    private boolean hasSourceField(final LeafClause clause) {
        return clause.getField() != null
                && clause.getField().equals(SESAM_SOURCE_FIELD);
    }
    
    private boolean containsJustThePrefix() {

        final LeafClause firstLeaf = context.getQuery().getFirstLeafClause();

        return context.getQuery().getRootClause() == firstLeaf
          && (firstLeaf.getKnownPredicates().contains(TokenPredicate.NEWS_MAGIC)
              || firstLeaf.getPossiblePredicates().contains(TokenPredicate.NEWS_MAGIC));
    }
    
    
    /**
     *
     * Visitor to create the FAST filter string. Handles the nyhetskilde: syntax.
     *
     * @todo add correct handling of NotClause and AndNotClause. This also needs
     * to be added to the query builder visitor above.
     *
     */
    private final class FilterVisitor extends AbstractReflectionVisitor {
        
        protected void visitImpl(final LeafClause clause) {
            if (hasSourceField(clause)) {
                appendSiteFilter(clause);
            }
        }
        
        protected void visitImpl(final PhraseClause clause) {
            if (hasSourceField(clause)) {
                appendSiteFilter(clause);
            }
        }
        
        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        
        protected void visitImpl(final OrClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        
        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        
        protected void visitImpl(final XorClause clause) {
            clause.getFirstClause().accept(this);
        }
        
        private final void appendSiteFilter(final LeafClause clause) {
            filterBuilder.append("+");
            filterBuilder.append(FAST_SOURCE_FILTER_FIELD);
            filterBuilder.append(':');
            filterBuilder.append(clause.getTerm().replaceAll("\"", ""));
        }
    }
}