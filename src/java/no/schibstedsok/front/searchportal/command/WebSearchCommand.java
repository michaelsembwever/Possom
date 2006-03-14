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
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.WordClause;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;
import org.apache.log4j.Logger;

/**
 *
 * A search command for the web search.
 *
 * @author magnuse
 */
public class WebSearchCommand extends FastSearchCommand {
    
    private static final String FAST_SITE_FILTER_PREFIX = "site";
    private static final String SESAM_SITE_PREFIX = "site";
    
    /** Creates a new instance of WebSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public WebSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }
    
    private StringBuffer filterBuilder = null;
    
    /**
     *
     * @param clause The clause to examine.
     */
    protected void visitImpl(final XorClause clause) {
        if (clause.getFirstClause() instanceof PhraseClause) {
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
        if (! hasSiteField(clause)) {
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
        if (! hasSiteField(clause)) {
            super.visitImpl(clause);
        }
    }
    
    protected String getAdditionalFilter() {
        synchronized(this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuffer();
                new FilterVisitor().visit(context.getQuery().getRootClause());
            }
            return filterBuilder.toString();
        }
    }
    
    private final boolean hasSiteField(final LeafClause clause) {
        return clause.getField() != null
                && clause.getField().equals(SESAM_SITE_PREFIX);
    }
    
    /**
     *
     * Visitor to create the FAST filter string. Handles the site: syntax.
     *
     * @todo add correct handling of NotClause and AndNotClause. This also needs
     * to be added to the query builder visitor above.
     *
     */
    private final class FilterVisitor extends AbstractReflectionVisitor {
        
        protected void visitImpl(final LeafClause clause) {
            if (hasSiteField(clause)) {
                appendSiteFilter(clause);
            }
        }
        
        protected void visitImpl(final PhraseClause clause) {
            if (hasSiteField(clause)) {
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
            filterBuilder.append(FAST_SITE_FILTER_PREFIX);
            filterBuilder.append(':');
            filterBuilder.append(clause.getTerm().replaceAll("\"", ""));
        }
    }
}
