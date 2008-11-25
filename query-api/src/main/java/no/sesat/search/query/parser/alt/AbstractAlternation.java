/* Copyright (2007-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 * AbstractAlternation.java
 *
 *
 */

package no.sesat.search.query.parser.alt;

import java.util.LinkedList;
import java.util.List;
import no.sesat.search.query.AndClause;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.BinaryOperatorClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.UnaryClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.finder.ChildFinder;
import org.apache.log4j.Logger;

/** Base abstraction class for any Alternation implementation.
 * Contains helper methods that are typically used within the alternation process.
 *  Some of these methods inturn delegate to visitor implementations found under the finder package.
 *
 *
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
    protected <T extends BinaryOperatorClause> T leftOpChild(final T clause){

        final Clause c = leftChild(clause);
        return clause.getClass().isAssignableFrom(c.getClass()) ? (T) c : null;
    }

    /** return the left child, left or operation.
     * @param clause
     * @return
     */
    protected Clause leftChild(final UnaryClause clause) {

        final Clause c = clause.getFirstClause();
        LOG.trace("leftChild -->" + c);
        return c;
    }

    /** will return null instead of a leafClause
     * @param clause
     * @return
     */
    protected <T extends BinaryOperatorClause> T rightOpChild(final T clause){

        final Clause c = rightChild(clause);
        return clause.getClass().isAssignableFrom(c.getClass()) ? (T) c : null;
    }

    /** will return right child, leaf or operation.
     * @param clause
     * @return
     */
    protected Clause rightChild(final BinaryOperatorClause clause) {

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
    protected <T extends UnaryClause> T parent(final T root, final Clause child) {

        final List<UnaryClause> parents = context.getParentFinder().getParents(root, child);
        T result = null;
        for(UnaryClause c : parents){
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
    protected <T extends UnaryClause> List<T> parents(final T root, final Clause child) {

        return (List<T>) context.getParentFinder().getParents(root, child);
    }

    /** Build new DoubleOperatorClauses from newChild all the way back up to the root.
     * XXX Only handles single splits, or one layer of variations, denoted by the originalParent argument.
     *      This could be solved by using an array, specifying ancestry line, for the argument instead. <br/><br/>
     * If, under root, originalParent cannot be found then root is returned unaltered.
     * @param root the root clause. an altered version of this will be returned.
     * @param newChild the new child.
     * @param originalChild the original child.
     * @param originalParent the original parent of the original child. expected to be found under root.
     * @return the root clause where the originalChild has been replaced with the newChild.
     */
    protected UnaryClause replaceDescendant(
            final BinaryOperatorClause root,
            final BinaryOperatorClause newChild,
            final BinaryOperatorClause originalChild,
            final BinaryOperatorClause originalParent){

        // pre-condition check: originalParent must be found under root somewhere
        if(new ChildFinder().childExists(root, originalParent)){

            UnaryClause nC = newChild;
            UnaryClause rC = originalChild;
            UnaryClause rCParent = originalParent;

            do{
                nC = replaceOperatorClause(nC, rC, rCParent);
                for(UnaryClause parent : context.getParentFinder().getParents(root, rC)){
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

        }else{
            LOG.error("originalParent does not live inside root\n" + originalParent + '\n' + root);
            // return the unaltered root
            return root;
        }
    }

    /** Replace the originalChild that exists under the originalParent will the newChild.
     *
     * @param newChild
     * @param originalChild
     * @param originalParent
     * @return
     */
    protected <T extends UnaryClause> T replaceOperatorClause(
            final Clause newChild,
            final Clause originalChild,
            final T originalParent) {

        final Clause leftChild = leftChild(originalParent) == originalChild
                        ? newChild
                        : leftChild(originalParent);

        final Clause rightChild;

        if(originalParent instanceof BinaryOperatorClause){

            rightChild = rightChild((BinaryOperatorClause)originalParent) == originalChild
                            ? newChild
                            : rightChild((BinaryOperatorClause)originalParent);
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
    protected <T extends UnaryClause> T createOperatorClause(
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
