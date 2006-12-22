// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import java.util.Map;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
public final class ExactTitleMatchQueryTransformer extends AbstractQueryTransformer {
    
    private static final Logger LOG = Logger.getLogger(ExactTitleMatchQueryTransformer.class);
    
    private transient boolean writtenStart = false;
    private transient boolean visitingLast = true;
    
    
    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final LeafClause clause) {
        
        if(!writtenStart){
            getTransformedTerms().put(clause, "titles:^\"" + getTransformedTerms().get(clause).replaceAll("^\"", ""));
            writtenStart = true;
        }
        if(visitingLast){
            getTransformedTerms().put(clause, getTransformedTerms().get(clause).replaceAll("\"$", "") + "\"$");
        }
    }


    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final DoubleOperatorClause clause) {
        visitingLast = false;
        clause.getFirstClause().accept(this);
        visitingLast = true;
        clause.getSecondClause().accept(this);
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }
    
    private Map<Clause,String> getTransformedTerms() {
        return getContext().getTransformedTerms();
    }
}
