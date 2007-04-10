// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import java.util.Map;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import org.apache.log4j.Logger;

/** Transforms the query into <br/>
 * titles:^"query"$
 * <br/>
 *   Ensures that only an exact match within the titles field is returned.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
public final class ExactMatchQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(ExactMatchQueryTransformer.class);

    private transient boolean writtenStart = false;
    private transient Boolean visitingLast = null;
    private ExactMatchQueryTransformerConfig config;

    /**
     *
     * @param config
     */
    public ExactMatchQueryTransformer(final QueryTransformerConfig config){
                
        this.config = (ExactMatchQueryTransformerConfig) config;
    }


    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final LeafClause clause) {

        if(!writtenStart){
            
            final String filter = null != config.getField()
                    ? config.getField() + ':'
                    : "";
            
            getTransformedTerms().put(
                    clause, 
                    filter + "^\"" + getTransformedTerms().get(clause).replaceAll("^\"", ""));
            
            writtenStart = true;
            // also, if we got here without giving visitingLast a value then this is the only LeafClause in the query
            visitingLast = null == visitingLast;
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

        // remember what visitingLast was
        final Boolean original = visitingLast;
        // turn it off. left child can never be the last term in the query.
        visitingLast = false;
        clause.getFirstClause().accept(this);
        // restore visitingLast.
        visitingLast = original;
        if( null == visitingLast ){
            //  if it is yet to be assigned an value (ie this is the topmost DoubleOperatorClause) then assign true.
            visitingLast = true;
        }
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
