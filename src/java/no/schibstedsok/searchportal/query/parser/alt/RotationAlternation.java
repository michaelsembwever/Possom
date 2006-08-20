/* Copyright (2005-2006) Schibsted Søk AS
 *
 * RotationAlternation.java
 *
 * Created on 4 March 2006, 11:51
 *
 */

package no.schibstedsok.searchportal.query.parser.alt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.parser.QueryParser;
import org.apache.log4j.Logger;

/** SEARCH-439 - Rotation Alternation.
 *
 * Variable notation:
 *  oXX -> original XX clause
 *  rXX -> XX clause from last rotation
 *  nXX -> newly rotated XX clause
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class RotationAlternation {

    /** Context to work within. **/
    public interface Context extends BaseContext, QueryParser.Context {  }

    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(RotationAlternation.class);
    private static final String INFO_ROTATIONS_RESULT = "RotationAlternation produced ";
    private static final String DEBUG_STARTING_ROTATIONS = "**** STARTING ROTATION ALTERNATION ****";
    private static final String DEBUG_FINISHED_ROTATIONS = "**** FINISHED ROTATION ALTERNATION ****";
    private static final String DEBUG_ROOT_NOT_OPERATION = "Root is not an OperationClause";
    private static final String DEBUG_FOUND_FORESTS = "Numer of forests found in query ";
    private static final String DEBUG_ORIGINAL_BRANCH_ADD = "Adding to original branch ";

    // Attributes ----------------------------------------------------

    private final Context context;
    private final ParentFinder parentFinder = new ParentFinder();

    /** mappings from the newly rotated clause to the same original clause **/
    private final Map<DoubleOperatorClause,DoubleOperatorClause> originalFromNew
            = new HashMap<DoubleOperatorClause,DoubleOperatorClause>();
    /** mappings from the original clause to the same newly rotated clause **/
    private final Map<DoubleOperatorClause,DoubleOperatorClause> newFromOriginal
            = new HashMap<DoubleOperatorClause,DoubleOperatorClause>();
    /** mappings from the newly rotated clause to the same unrotated clause **/
    private final Map<DoubleOperatorClause,DoubleOperatorClause> beforeRotationFromNew
            = new HashMap<DoubleOperatorClause,DoubleOperatorClause>();
    /** mappings from the original to the unrotated clause */
    private final Map<DoubleOperatorClause,DoubleOperatorClause> beforeRotationFromOriginal
            = new HashMap<DoubleOperatorClause,DoubleOperatorClause>();

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of RotationAlternation.
     */
    public RotationAlternation(final Context cxt) {
        context = cxt;
    }

    // Public --------------------------------------------------------

    /** TODO comment me. **/
    public Clause createRotations(final Clause originalRoot) {

        // find forests (subtrees) of AndClauses and OrClauses.
        // TODO handle forests hidden behind SingleOperatorClauses (NOT and ANDNO)
        //  although queries rarely start with such clauses.
        // XXX This implementation only handles forests that exist down the right branch only.
        if(originalRoot instanceof DoubleOperatorClause){

            LOG.debug(DEBUG_STARTING_ROTATIONS);
            DoubleOperatorClause root = (DoubleOperatorClause) originalRoot;

            final List<DoubleOperatorClause> forestRoots = new ForestFinder().findForestRoots(root);
            LOG.debug(DEBUG_FOUND_FORESTS + forestRoots.size());

            for(DoubleOperatorClause clause : forestRoots){

                final LinkedList<? extends DoubleOperatorClause> rotations = createForestRotation(clause);

                final XorClause result = createXorClause(rotations);
                // search in root for all occurances of clause and 'replaceDescendant' on each.
                if(root == clause){
                    root = result;
                }else{
                    // search in root for all occurances of clause and 'replaceDescendant' on each.
                    final List<DoubleOperatorClause> parents = parents(root, clause);
                    for(DoubleOperatorClause clauseParent : parents){
                        root = replaceDescendant(root, result, clause, clauseParent);
                    }
                }

                originalFromNew.clear();
                newFromOriginal.clear();
                beforeRotationFromNew.clear();
                beforeRotationFromOriginal.clear();
            }
            LOG.info(INFO_ROTATIONS_RESULT + root);
            LOG.debug(DEBUG_FINISHED_ROTATIONS);

            return root;
        }
        return originalRoot;
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private <T extends DoubleOperatorClause> LinkedList<T> createForestRotation(
            final T oForestRoot) {

        LOG.debug("==== STARTING ROTATION ON " + oForestRoot + " ====");

        // store this right-leaning branch for later comparason.
        final LinkedList<T> origBranch = new LinkedList<T>();
        for (T oC = oForestRoot; oC != null; oC = rightOpChild(oC)) {

            // add to branch
            LOG.debug(DEBUG_ORIGINAL_BRANCH_ADD + oC);
            origBranch.add(oC);
            // add to the state-memory maps (creating initial map state, simple self pointing mappings)
            originalFromNew.put(oC, oC);
            beforeRotationFromNew.put(oC, oC);
            beforeRotationFromOriginal.put(oC, oC);
        }

        // the size of the original right-leaning branch is also the number of alternations
        final LinkedList<T> alternations = new LinkedList<T>();
        // and the first nAlternation is the original branch
        alternations.addFirst(oForestRoot);

        // oIterate backwards from the right-most child (of type rotateFor) on the branch
        for (T oIterate = origBranch.removeLast(); origBranch.size() > 0; oIterate = origBranch.removeLast()) {

            // clear mappings
            beforeRotationFromOriginal.clear();
            for (Entry<DoubleOperatorClause,DoubleOperatorClause> entry : beforeRotationFromNew.entrySet()) {

                // reverse key to values in each entry
                // entry.getValue() is NOT new!!
                beforeRotationFromOriginal.put(originalFromNew.get(entry.getKey()), entry.getKey());
            }
            originalFromNew.clear();
            newFromOriginal.clear();
            beforeRotationFromNew.clear();

            // find the right-most parent of iteration clause
            final T rLastestForestRoot = (T) beforeRotationFromOriginal.get(oForestRoot);
            T rBottom = (T) beforeRotationFromOriginal.get(oIterate);
            while(rBottom == leftChild(parent(rLastestForestRoot, rBottom))){
                rBottom = parent(rLastestForestRoot, rBottom);
            }

            // from 'rBottom' move upwards to the left,
            //  continue repeating if next parent does not have parent to the right
            T rTop = rBottom;
            T rTopParent = rLastestForestRoot == rTop ? null : parent(rLastestForestRoot, rTop);
            while(rTopParent != null
                    && (rLastestForestRoot == rTopParent
                        || rTopParent != leftChild(parent(rLastestForestRoot, rTopParent)))){

                rTop = rTopParent;
                rTopParent = rLastestForestRoot == rTop ? null : parent(rLastestForestRoot, rTop);
            }

            // we can rotate these now
            final T nAlternation = rotate(oForestRoot, oIterate, rTop, rBottom);
            alternations.addLast(nAlternation);
        }

        LOG.debug("==== FINISHED ROTATION ON " + oForestRoot + " ====");
        return alternations;
    }


    private <T extends DoubleOperatorClause> T rotate(
            final T oForestRoot, // from original
            final T oIterate,  // from original
            final T rTop,  // from last rotation
            final T rBottom) { // from last rotation

        LOG.debug("rotate(" + oForestRoot + ", " + oIterate + ", " + rTop + ", " + rBottom + ")");

        // RIGHT-LEANING-LOWER BRANCH ROTATION
        LOG.debug("--- RIGHT-LEANING-LOWER BRANCH ROTATION ---");
        // re-construct the branch starting at the oIterate
        // the orpan must be from the newly rotated branch. (not the original or the last rotated branch).
        // the first nOrphan is the exception because it is always a leaf that's free to re-use.
        Clause nOrphan = leftChild(beforeRotationFromOriginal.get(oIterate));

        T oC = oIterate;
        do{
            oC = parent(oForestRoot, oC);
            LOG.debug(" orpan--> " + nOrphan);
            LOG.debug(" c--> " + oC);
            // oC is actually from the original branch.
            //  But it doesn't matter because the left child is a leaf that's free to re-use.
            nOrphan = createOperatorClause(leftChild(oC), nOrphan, oC);
            LOG.debug("  result--> " + nOrphan);

        }while(beforeRotationFromOriginal.get(oC) != rTop); // we have just rotated the rTop clause. getthefuckout.

        // LEFT-LEANING-UPPER-BRANCH ROTATION
        //  rotate what's now between the nOrphan and rBottom
        LOG.debug("--- LEFT-LEANING-UPPER-BRANCH ROTATION ---");
        oC = rightOpChild((T)originalFromNew.get(nOrphan));
        // first find the first right child that's not yet been orphaned.
        while (newFromOriginal.get(oC) != null) {
            oC = rightOpChild(oC);
        }

        // re-construct the left-leaning tail branch
        do{

            LOG.debug(" orphan--> " + nOrphan);
            LOG.debug(" c--> " + oC);
            // oC is actually from the original branch.
            final T rC = (T) beforeRotationFromOriginal.get(oC);
            nOrphan = createOperatorClause(nOrphan, rightChild(rC), oC);
            LOG.debug("  result--> " + nOrphan);
            oC = rightOpChild(oC);

        }while(oC != null); // we have just rotated the rBottom. getthefuckout.

        // ORIGINAL TREE ROOT ROTATION
        LOG.debug("--- ORIGINAL TREE ROOT ROTATION ---");
        // keep rotating above the centre of rotation
            // loop rebuilding the tree, only replacing old instances with new instances.
        final T rForestRoot = (T) beforeRotationFromOriginal.get(oForestRoot);
        if(beforeRotationFromNew.size() != beforeRotationFromOriginal.size()){
            nOrphan = replaceDescendant(rForestRoot, (DoubleOperatorClause) nOrphan, rTop, parent(rForestRoot,rTop)); // XXX last argument needs to be from orginal branch
        }
        return (T) nOrphan;
    }

    /** will return null instead of a leafClause **/
    private <T extends DoubleOperatorClause> T leftOpChild(final T clause){

        final Clause c = leftChild(clause);
        return clause.getClass().isAssignableFrom(c.getClass()) ? (T) c : null;
    }

    /** return the left child, left or operation. **/
    private Clause leftChild(final DoubleOperatorClause clause) {

        final Clause c = clause.getFirstClause();
        LOG.trace("leftChild -->" + c);
        return c;
    }

    /** will return null instead of a leafClause **/
    private <T extends DoubleOperatorClause> T rightOpChild(final T clause){

        final Clause c = rightChild(clause);
        return clause.getClass().isAssignableFrom(c.getClass()) ? (T) c : null;
    }

    /** will return right child, leaf or operation. **/
    private Clause rightChild(final DoubleOperatorClause clause) {

        final Clause c = clause.getSecondClause();
        LOG.trace("rightChild -->" + c);
        return c;
    }

    /** return the parent operation clause of the given child.
     * And the child must be a descendant of the root. **/
    private <T extends DoubleOperatorClause> T parent(final T root, final Clause child) {

        return parentFinder.getParent(root, child);
    }

    /** return all parents operation clauses of the given child. **/
    private <T extends DoubleOperatorClause> List<T> parents(final T root, final Clause child) {

        return parentFinder.getParents(root, child);
    }

    /** Build new DoubleOperatorClauses from newChild all the way back up to the root.
     * XXX Only handles single splits, or one layer of variations, denoted by the childsParentBeforeRotation argument.
     *      This could be solved by using an array, specifying ancestry line, for the argument instead.
     **/
    private DoubleOperatorClause replaceDescendant(
            final DoubleOperatorClause root,
            final DoubleOperatorClause newChild,
            final DoubleOperatorClause childBeforeRotation,
            final DoubleOperatorClause childsParentBeforeRotation){

        DoubleOperatorClause nC = newChild;
        DoubleOperatorClause rC = childBeforeRotation;
        DoubleOperatorClause rCParent = childsParentBeforeRotation;

        do{
            nC = replaceOperatorClause(root, nC, rC, rCParent);
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

    private <T extends DoubleOperatorClause> T replaceOperatorClause(
            final DoubleOperatorClause root,
            final Clause newChild,
            final T childBeforeRotation,
            final DoubleOperatorClause childsParentBeforeRotation) {

        return createOperatorClause(
                    leftChild(childsParentBeforeRotation) == childBeforeRotation
                        ? newChild
                        : leftChild(childsParentBeforeRotation),
                    rightChild(childsParentBeforeRotation) == childBeforeRotation
                        ? newChild
                        : rightChild(childsParentBeforeRotation),
                    (T)childsParentBeforeRotation); // XXX last argument needs to be from orginal branch
    }

    /** Create a new operator clause, of type opCls, with the left and right children.
     * We must also specify for whom it is to be a replacement for.
     * The replacementFor must be from the original branch.
     **/
    private <T extends DoubleOperatorClause> T createOperatorClause(
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
        // update our mappings between rotations
        originalFromNew.put(clause, replacementFor);
        newFromOriginal.put(replacementFor, clause);
        beforeRotationFromNew.put(clause, beforeRotationFromOriginal.get(replacementFor));

        return clause;
    }

    private XorClause createXorClause(final LinkedList<? extends DoubleOperatorClause> rotations){

        return context.createXorClause(
                rotations.removeLast(),
                rotations.size() == 1 ? rotations.removeLast() : createXorClause(rotations),
                XorClause.ROTATION_ALTERNATION);
    }

    /** TODO comment me. **/
    public Map<DoubleOperatorClause, DoubleOperatorClause> getBeforeRotationFromNew() {
        return beforeRotationFromNew;
    }

    // Inner classes -------------------------------------------------

    private static final class ParentFinder extends AbstractReflectionVisitor {

        private boolean searching = false;
        private boolean singleMode = false;
        private List<DoubleOperatorClause> parents = new ArrayList<DoubleOperatorClause>();
        private Clause child;
        private final Map<DoubleOperatorClause,Map<Clause,List<DoubleOperatorClause>>> cache
                = new HashMap<DoubleOperatorClause,Map<Clause,List<DoubleOperatorClause>>>();

        private static final String ERR_CANNOT_CALL_VISIT_DIRECTLY
                = "visit(object) can't be called directly on this visitor!";
        private static final String ERR_CHILD_NOT_IN_HEIRARCHY
                = "The child is not part of this clause family!";

        private synchronized <T extends DoubleOperatorClause> void findParentsImpl(final T root, final Clause child) {

            this.child = child;
            if (searching || child == null) {
                throw new IllegalStateException(ERR_CANNOT_CALL_VISIT_DIRECTLY);
            }
            searching = true;
            parents.clear();
            if(null == findInCache(root)){
                visit(root);
                updateCache(root);
            }else{
                parents.addAll(findInCache(root));
            }
            searching = false;
            this.child = null;
        }

        public synchronized <T extends DoubleOperatorClause> List<T> getParents(final T root, final Clause child) {
            findParentsImpl(root, child);
            return Collections.unmodifiableList(new ArrayList<T>((List<T>)parents));
        }

        public synchronized <T extends DoubleOperatorClause> T getParent(final T root, final Clause child) {

            singleMode = true;
            findParentsImpl(root, child);
            singleMode = false;
            if (parents.size() == 0) {
                throw new IllegalArgumentException(ERR_CHILD_NOT_IN_HEIRARCHY);
            }
            return (T)parents.get(0);
        }

        private <T extends DoubleOperatorClause> List<DoubleOperatorClause> findInCache(final T root){

            Map<Clause,List<DoubleOperatorClause>> innerCache = cache.get(root);
            if(innerCache == null){
                innerCache = new HashMap<Clause,List<DoubleOperatorClause>>();
                cache.put(root, innerCache);
            }
            return innerCache.get(child);
        }

        private <T extends DoubleOperatorClause> void updateCache(final T root){

            Map<Clause,List<DoubleOperatorClause>> innerCache = cache.get(root);
            if(innerCache == null){
                innerCache = new HashMap<Clause,List<DoubleOperatorClause>>();
                cache.put(root, innerCache);
            }
            innerCache.put(child, new ArrayList<DoubleOperatorClause>(parents));
        }

        protected void visitImpl(final OperationClause clause) {
            if (!singleMode || parents.size() == 0) {
                clause.getFirstClause().accept(this);
            }
        }

        protected void visitImpl(final DoubleOperatorClause clause) {
            if (!singleMode || parents.size() == 0) {
                if (clause.getFirstClause() == child || clause.getSecondClause() == child) {
                    parents.add(clause);
                }  else  {
                    clause.getFirstClause().accept(this);
                    clause.getSecondClause().accept(this);
                }
            }
        }

        protected void visitImpl(final LeafClause clause) {
            // leaves can't be parents :-)
        }

    }

    private static final class ForestFinder extends AbstractReflectionVisitor {


        private static final Logger LOG = Logger.getLogger(ForestFinder.class);
        private static final String DEBUG_COUNT_TO = " trees in forest ";
        private boolean searching = false;
        private final List<DoubleOperatorClause> roots = new ArrayList<DoubleOperatorClause>();

        private static final String ERR_CANNOT_CALL_VISIT_DIRECTLY
                = "visit(object) can't be called directly on this visitor!";

        public synchronized List<DoubleOperatorClause> findForestRoots(final OperationClause root) {

            if (searching) {
                throw new IllegalStateException(ERR_CANNOT_CALL_VISIT_DIRECTLY);
            }
            searching = true;
            roots.clear();
            visit(root);
            //Collections.reverse(roots);
            searching = false;
            return Collections.unmodifiableList(roots);
        }


        protected void visitImpl(final OperationClause clause) {

            clause.getFirstClause().accept(this);
        }

        protected void visitImpl(final XorClause clause) {

            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final DoubleOperatorClause clause) {

            final DoubleOperatorClause forestDepth = forestWalk(clause);
            clause.getFirstClause().accept(this);
            forestDepth.getSecondClause().accept(this);
        }

//        protected void visitImpl(final AndClause clause) {
//
//            final AndClause forestDepth = forestWalk(clause);
//            clause.getFirstClause().accept(this);
//            forestDepth.getSecondClause().accept(this);
//        }
//
//        protected void visitImpl(final DefaultOperatorClause clause) {
//
//            final DefaultOperatorClause forestDepth = forestWalk(clause);
//            clause.getFirstClause().accept(this);
//            forestDepth.getSecondClause().accept(this);
//        }

        protected void visitImpl(final LeafClause clause) {
            // leaves can't be forest roots :-)
        }

        /** Returns the deepest tree in the forest.
         * And adds the forest to the roots if it contains more than one tree.
         **/
        private <T extends DoubleOperatorClause> T forestWalk(final T clause){

            int count = 1;
            T forestDepth = clause;
            // presumption below is that forests can't mix implementation classes, not just interfaces.
            for( ;  forestDepth.getSecondClause().getClass() == clause.getClass();
                    forestDepth = (T) forestDepth.getSecondClause()){
                ++count;
            }
            LOG.debug(count + DEBUG_COUNT_TO + clause);
            if(count >1){
                roots.add(clause);
            }
            return forestDepth;
        }

    }

}
