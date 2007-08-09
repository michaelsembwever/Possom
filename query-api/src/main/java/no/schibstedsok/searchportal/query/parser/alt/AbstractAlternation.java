/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 * AbstractAlternation.java
 *
 *
 */

package no.schibstedsok.searchportal.query.parser.alt;

import java.util.LinkedList;
import java.util.List;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.XorClause;
import org.apache.log4j.Logger;

/** Base abstraction class for any Alternation implementation.
 * Contains helper methods that are typically used within the alternation process.
 *  Some of these methods inturn delegate to visitor implementations found under the finder package.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public abstract class AbstractAlternation implements Alternation{
    
    // Constants -----------------------------------------------------  
    
    private static final Logger LOG = Logger.getLogger(AbstractAlternation.class);
    
    private static final String ERR_MULTIPLE_POSSIBLE_PARENTS = "Multiple parents exist with same (or sub) class as ";
    
    // Attributes ----------------------------------------------------
    
    /**
     * The context to work within.
     */
    protected final Context context;    
    
    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of AbstractAlternation 
     * @param cxt 
     */
    public AbstractAlternation(final Context cxt) {
        context = cxt;
    }
    
    // Public --------------------------------------------------------
    
    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------
    

    /** will return null instead of a leafClause
     * @param clause 
     * @return 
     */
    protected <T extends DoubleOperatorClause> T leftOpChild(final T clause){

        final Clause c = leftChild(clause);
        return clause.getClass().isAssignableFrom(c.getClass()) ? (T) c : null;
    }

    /** return the left child, left or operation.
     * @param clause 
     * @return 
     */
    protected Clause leftChild(final OperationClause clause) {

        final Clause c = clause.getFirstClause();
        LOG.trace("leftChild -->" + c);
        return c;
    }

    /** will return null instead of a leafClause
     * @param clause 
     * @return 
     */
    protected <T extends DoubleOperatorClause> T rightOpChild(final T clause){

        final Clause c = rightChild(clause);
        return clause.getClass().isAssignableFrom(c.getClass()) ? (T) c : null;
    }

    /** will return right child, leaf or operation.
     * @param clause 
     * @return 
     */
    protected Clause rightChild(final DoubleOperatorClause clause) {

        final Clause c = clause.getSecondClause();
        LOG.trace("rightChild -->" + c);
        return c;
    }

    /** return the parent operation clause of the given child.
     * And the child must be a descendant of the root.
     * The result will also be assignable from the root argument's class.
     * If there exists multiple parents all of the required class an IllegalStateException is thrown.
     * @param child 
     * @param root 
     */
    protected <T extends OperationClause> T parent(final T root, final Clause child) {

        final List<OperationClause> parents = context.getParentFinder().getParents(root, child);
        T result = null;
        for(OperationClause c : parents){
            if(root.getClass().isAssignableFrom(c.getClass())){
                if(null != result){
                    throw new IllegalStateException(ERR_MULTIPLE_POSSIBLE_PARENTS + root.getClass());
                }
                result = (T)c;
            }
        }
        return result;
    }

    /** return all parents operation clauses of the given child.
     * @param root 
     * @param child 
     * @return 
     */
    protected <T extends OperationClause> List<T> parents(final T root, final Clause child) {

        return (List<T>) context.getParentFinder().getParents(root, child);
    }

    /** Build new DoubleOperatorClauses from newChild all the way back up to the root.
     * XXX Only handles single splits, or one layer of variations, denoted by the childsParentBeforeRotation argument.
     *      This could be solved by using an array, specifying ancestry line, for the argument instead.
     ** @param root 
     * @param newChild 
     * @param originalChild 
     * @param originalParent 
     * @return 
     */
    protected OperationClause replaceDescendant(
            final DoubleOperatorClause root,
            final DoubleOperatorClause newChild,
            final DoubleOperatorClause originalChild,
            final DoubleOperatorClause originalParent){

        OperationClause nC = newChild;
        OperationClause rC = originalChild;
        OperationClause rCParent = originalParent;

        do{
            nC = replaceOperatorClause(nC, rC, rCParent);
            for(OperationClause parent : context.getParentFinder().getParents(root, rC)){
                if(rCParent == parent){
                    rC = parent;
                    rCParent = root == rCParent 
                            ? rCParent 
                            : context.getParentFinder().getParent(root, rCParent);
                    break;
                }
            }
        }while(root != rC);

        return nC;
    }

    /** Replace the originalChild that exists under the originalParent will the newChild.
     * 
     * @param newChild 
     * @param originalChild 
     * @param originalParent 
     * @return 
     */
    protected <T extends OperationClause> T replaceOperatorClause(
            final Clause newChild,
            final Clause originalChild,
            final T originalParent) {
        
        final Clause leftChild = leftChild(originalParent) == originalChild
                        ? newChild
                        : leftChild(originalParent);
        
        final Clause rightChild;
        
        if(originalParent instanceof DoubleOperatorClause){
            
            rightChild = rightChild((DoubleOperatorClause)originalParent) == originalChild
                            ? newChild
                            : rightChild((DoubleOperatorClause)originalParent);
        }else{
            rightChild = null;
        }

        // XXX last argument needs to be from original branch
        return createOperatorClause(leftChild, rightChild, originalParent);
    }

    /** Create a new operator clause, of type opCls, with the left and right children.
     * We must also specify for whom it is to be a replacement for.
     * The replacementFor must be from the original branch.
     ** @param left 
     * @param right 
     * @param replacementFor 
     * @return 
     */
    protected <T extends OperationClause> T createOperatorClause(
            final Clause left,
            final Clause right,
            final T replacementFor) {

        LOG.debug("createOperatorClause(" + left + ", " + right + ", " + replacementFor + ")");
        T clause = null;

        if (AndClause.class.isAssignableFrom(replacementFor.getClass())) {
            clause = (T) context.createAndClause(left, right);

        } else if (XorClause.class.isAssignableFrom(replacementFor.getClass())) {
            clause = (T) context.createXorClause(left, right, ((XorClause)replacementFor).getHint());

        } else if (OrClause.class.isAssignableFrom(replacementFor.getClass())) {
            clause = (T) context.createOrClause(left, right);

        } else if (DefaultOperatorClause.class.isAssignableFrom(replacementFor.getClass())) {
            clause = (T) context.createDefaultOperatorClause(left, right);

        }else if (NotClause.class.isAssignableFrom(replacementFor.getClass())){
            clause = (T) context.createNotClause(left);
            
        }else if (AndNotClause.class.isAssignableFrom(replacementFor.getClass())){
            clause = (T) context.createAndNotClause(left);
            
        }

        return clause;
    }

    /** Create XorClauses required to present all the alternatives in the query tree.
     * There will be alternatives.size()-1 XorClauses aligned in a right-leaning branch.
     * 
     * @param alternatives what will be leaves of the right-leaning XorClause branch returned
     * @return the right-leaning XorClause branch
     */
    protected XorClause createXorClause(final LinkedList<? extends Clause> alternatives){

        return context.createXorClause(
                alternatives.removeLast(),
                alternatives.size() == 1 ? alternatives.removeLast() : createXorClause(alternatives),
                getAlternationHint());
    }

    /** What XorClause.Hint is used for newly created XorClause alternations.
     * 
     * @return the XorClause.Hint used during this alternation process.
     */
    protected abstract XorClause.Hint getAlternationHint();
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}
