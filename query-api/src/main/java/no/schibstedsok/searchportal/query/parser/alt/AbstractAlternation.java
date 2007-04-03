/*
 * AbstractAlternation.java
 *
 *
 */

package no.schibstedsok.searchportal.query.parser.alt;

import java.util.LinkedList;
import java.util.List;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.XorClause;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public abstract class AbstractAlternation implements Alternation{
    
    // Constants -----------------------------------------------------  
    
    private static final Logger LOG = Logger.getLogger(AbstractAlternation.class);
    
    // Attributes ----------------------------------------------------
    
    /**
     * 
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
    

    /** will return null instead of a leafClause *
     * @param clause 
     * @return 
     */
    protected <T extends DoubleOperatorClause> T leftOpChild(final T clause){

        final Clause c = leftChild(clause);
        return clause.getClass().isAssignableFrom(c.getClass()) ? (T) c : null;
    }

    /** return the left child, left or operation. *
     * @param clause 
     * @return 
     */
    protected Clause leftChild(final DoubleOperatorClause clause) {

        final Clause c = clause.getFirstClause();
        LOG.trace("leftChild -->" + c);
        return c;
    }

    /** will return null instead of a leafClause *
     * @param clause 
     * @return 
     */
    protected <T extends DoubleOperatorClause> T rightOpChild(final T clause){

        final Clause c = rightChild(clause);
        return clause.getClass().isAssignableFrom(c.getClass()) ? (T) c : null;
    }

    /** will return right child, leaf or operation. *
     * @param clause 
     * @return 
     */
    protected Clause rightChild(final DoubleOperatorClause clause) {

        final Clause c = clause.getSecondClause();
        LOG.trace("rightChild -->" + c);
        return c;
    }

    /** return the parent operation clause of the given child.
     * And the child must be a descendant of the root. ** @param root 
     * @param child 
     * @param root 
     */
    protected <T extends OperationClause> T parent(final T root, final Clause child) {

        return (T) context.getParentFinder().getParent(root, child);
    }

    /** return all parents operation clauses of the given child. *
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
    protected DoubleOperatorClause replaceDescendant(
            final DoubleOperatorClause root,
            final DoubleOperatorClause newChild,
            final DoubleOperatorClause originalChild,
            final DoubleOperatorClause originalParent){

        DoubleOperatorClause nC = newChild;
        DoubleOperatorClause rC = originalChild;
        DoubleOperatorClause rCParent = originalParent;

        do{
            nC = replaceOperatorClause(nC, rC, rCParent);
            for(DoubleOperatorClause parent : parents(root, rC)){
                if(rCParent == parent){
                    rC = parent;
                    rCParent = root == rCParent ? rCParent : parent(root, rCParent);
                    break;
                }
            }
        }while(root != rC);

        return nC;
    }

    /**
     * 
     * @param newChild 
     * @param originalChild 
     * @param originalParent 
     * @return 
     */
    protected <T extends DoubleOperatorClause> T replaceOperatorClause(
            final Clause newChild,
            final T originalChild,
            final DoubleOperatorClause originalParent) {

        return createOperatorClause(
                    leftChild(originalParent) == originalChild
                        ? newChild
                        : leftChild(originalParent),
                    rightChild(originalParent) == originalChild
                        ? newChild
                        : rightChild(originalParent),
                    (T)originalParent); // XXX last argument needs to be from original branch
    }

    /** Create a new operator clause, of type opCls, with the left and right children.
     * We must also specify for whom it is to be a replacement for.
     * The replacementFor must be from the original branch.
     ** @param left 
     * @param right 
     * @param replacementFor 
     * @return 
     */
    protected <T extends DoubleOperatorClause> T createOperatorClause(
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

        }

        return clause;
    }

    /**
     * 
     * @param alternatives 
     * @return 
     */
    protected XorClause createXorClause(final LinkedList<? extends DoubleOperatorClause> alternatives){

        return context.createXorClause(
                alternatives.removeLast(),
                alternatives.size() == 1 ? alternatives.removeLast() : createXorClause(alternatives),
                getAlternationHint());
    }

    /** What XorClause.Hint is used for newly created XorClause alternations.
     * 
     * @return 
     */
    protected abstract XorClause.Hint getAlternationHint();
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}
