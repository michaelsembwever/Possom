/* Copyright (2005-2008) Schibsted ASA
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
 *
 * RotationAlternation.java
 *
 * Created on 4 March 2006, 11:51
 *
 */

package no.sesat.search.query.parser.alt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import no.sesat.search.query.Clause;
import no.sesat.search.query.BinaryClause;
import no.sesat.search.query.UnaryClause;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.XorClause.Hint;
import no.sesat.search.query.finder.Counter;
import no.sesat.search.query.finder.ForestFinder;
import org.apache.log4j.Logger;

/** <b><a href="http://sesat.no/scarab/issues/id/SKER439">SKER-439 - Rotation Alternation</a></b>
 *
 * <p>Rotates like-joined DoubleOperatorClauses (forests).
 * Tree rotation on the Query clause heirarchy is expensive,
 * and as clauses are immutable and are reused through a weak-referenced cache map,
 * performing and remembering the rotations directly after tree construction
 * is a performance benefit over repeated on-the-fly rotations.<br/>
 * The memory increase, taking into account the weak-reference maps is (N-1)^2 +2N -2,
 *  where N is the number leaf clauses in the forest.
 * From this it can also be shown that the asymptotic complexity is just shy of O(n^2).<br/>
 * XorClauses with Hint.ROTATION_ALTERNATION are used to store these alternations within the query tree.</p>
 * <p>
 * Typical usecases of these Rotation-variant XorClauses are visitors that are
 *  interested in possible combinations, or joins, between leaf clauses,
 *   for example the query evaluation and analysis, and the synonym query transformer.</p>
 *
 * <p>Here's an example of the sequencial rotations on a query clause heirarchy of eight leaf clauses,
 *  all eight leaf clauses are joined by DefaultOperatorClause,
 *   so there is only one forest to perform rotations on.<br/>
 * The numbers one through to eight represent the eight terms in the query string and hence the eight leaf clauses.
 *  In every rotation these leaf clauses refer to the same reused immutable instances.<br/>
 * The letters A through to G represent the DoubleOperatorClauses.
 *  In every rotation these are always different instances as their layout of grandchildren differ.<br/>
 * The number of rotations in any forest is always one less than the number of DoubleOperatorClauses in the forest.<br/>
 * The leaf clauses from left to right always remain in order
 *  so visitors can be assured that the query string at large remains the same.</p>
 *
 * <img src="doc-files/RotationAlternation-1.png"/>
 *
 * <p>To perform each rotation, the iterate, top, bottom, and orphan clause must be all determined.
 *  This is indicated in the diagram on the forest before the rotation occurs.
 * <ul>
 * <li>The iterate starts at the deepest clause and works itself back one each rotation,
 *  eg G for the first rotation process, F for the second rotation process, etc.</li>
 * <li>The bottom clause is the right-most parent of the iterate clause.</li>
 * <li>The top clause is the left-most parent of the bottom clause
 *  where its parent inturn does not have parent to the right.
 *   There is an exception here, it is allowed for the top clause's parent to be on the right-side
 *    when the top is the direct left parent to the bottom.</li>
 * <li>The orphan clause is the left child leaf clause to the deepest double operator clause.
 * </ul>
 * The process of rotation is then split into three steps, pre-rotation, post-rotation, and centre-of-rotation.
 *  This is indicated in the diagram on the forest after it has been rotated.
 * <ul>
 * <li>Pre-Rotation, or 'RIGHT-LEANING-LOWER BRANCH ROTATION', is responsible for rotating the double operator clauses
 *  from the bottom's parent up til and including the top clause.</li>
 * <li> Post-Rotation, or 'LEFT-LEANING-UPPER-BRANCH ROTATION', is the rotation from the orphan's parent
 *  (the double operator clause), which is to become the top's (in it's new position) new parent, up til and including
 *  the bottom clause.</li>
 * <li> Centre-of-Rotation, or 'ORIGINAL TREE ROOT ROTATION', is the reconstruction of the forest above the bottom's
 *  new position up til and including the root clause of the forest. This section remains in appearance the same as the
 *  previous rotation but must be reconstructed due to differences in how the grandchildren are laid out.
 * </ul>
 * </p>
 *<br/><br/>
 * <p>
 * <b>Example usecases of the resulting rotations within the query</b><br/>
 * The questions begs when is this rotation actually used and to what benefit?<br/>
 * <ul>
 * <li> Query {@link no.sesat.search.query.analyser Analysis} relies on all possible term combinations to
 *          exist with their corresponding tokenPredicates
 *          (which must be exact on the boundaries). The analysis does one complete sweep of the query tree, including
 *          the rotations, looking for tokenPredicates inaccordance with the rules to create the scores for each rule.
 * </li>
 * <li> The {@link no.sesat.search.query.finder.ParentFinder ParentFinder} class, a utility visitor
 *              implementation, contains the method getParents(root, clause).<br/>
 *          While this visitor does not explicitly use the XorClause.ROTATION_ALTERNATION, it simply visits down all
 *              branches of the query returning clauses it comes across that a child matching the clause argument.<br/>
 *          Through this utility it is possible to find variations of combinations that a term in the query
 *              can be applicable to.
 * <li> {@link no.sesat.search.query.transform.SynonymQueryTransformer SynonymQueryTransformer}.
 *          Has not yet been fully rewritten to utilise the rotations.
 *          <a href="http://sesat.no/scarab/issues/id/SKER863"
 *                  >SKER863 - Rewrite SynonymQueryTransformer to use RotationAlternation</a><br/>
 *          But it does already use the ParentFinder visitor utility to check if a given clause is inside a given
 *          tokenPredicate.
 * </li>
 * <li> {@link no.sesat.search.query.finder.WhoWhereSplitter WhoWhereSplitter} is a utility vistor that
 *              splits the query into 'where' and 'who' components.<br/>
 *          It relies heavily upon tokenPredicates linked to fast-lists from the query matching servers.<br/>
 *          Since the where component is often defined by either a fullname or companyname predicate which is generally
 *              multi-worded, so the class uses the ParentFinder to check if a given clause is inside one of these given
 *              tokenPredicates. The use of the ParentFinder is extended within the private method parentsOf(clause).
 * </li>
 * </ul>
 * </p>
 * <br/><br/>
 *
 * <b>References:</b><br/>
 * <a href="http://www.cs.queensu.ca/home/jstewart/applets/bst/bst-rotation.html">Binary Tree Rotation</a><br/>
 * <a href="http://www.nist.gov/dads/HTML/rotation.html">Institute of Standards and Technology - Rotation</a><br/>
 * <a href="http://en.wikipedia.org/wiki/Expressed_sequence_tag">Sequence tagging</a><br/>
 *
 *
 * @version $Id$
 *
 *
 */
public final class RotationAlternation extends AbstractAlternation{

    /*
     * Variable notation:
     *  oXX -> original XX clause
     *  rXX -> XX clause from last rotation
     *  nXX -> newly rotated XX clause
     */


    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(RotationAlternation.class);
    private static final String INFO_ROTATIONS_RESULT = "RotationAlternation produced ";
    private static final String DEBUG_STARTING_ROTATIONS = "**** STARTING ROTATION ALTERNATION ****";
    private static final String DEBUG_FINISHED_ROTATIONS = "**** FINISHED ROTATION ALTERNATION ****";
    private static final String DEBUG_FOUND_FORESTS = "Numer of forests found in query ";
    private static final String DEBUG_ORIGINAL_BRANCH_ADD = "Adding to original branch ";

    // Attributes ----------------------------------------------------


    /** mappings from the newly rotated clause to the same original clause **/
    private final Map<BinaryClause,BinaryClause> originalFromNew
            = new HashMap<BinaryClause,BinaryClause>();
    /** mappings from the original clause to the same newly rotated clause **/
    private final Map<BinaryClause,BinaryClause> newFromOriginal
            = new HashMap<BinaryClause,BinaryClause>();
    /** mappings from the newly rotated clause to the same unrotated clause **/
    private final Map<BinaryClause,BinaryClause> beforeRotationFromNew
            = new HashMap<BinaryClause,BinaryClause>();
    /** mappings from the original to the unrotated clause */
    private final Map<BinaryClause,BinaryClause> beforeRotationFromOriginal
            = new HashMap<BinaryClause,BinaryClause>();

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of RotationAlternation.
     * @param cxt
     */
    public RotationAlternation(final Context cxt) {
        super(cxt);
    }

    // Public --------------------------------------------------------

    /** Returns a alternated clause where all child forests contain XorClauses.
     * These XorClauses hold all possible rotations for the corresponding forest.
     * Each XorClause always puts the left-leaning rotation as the left child,
     *  and the right-leaning rotation (or the next XorClause) as the right child.
     ** @param originalRoot
     */
    public Clause alternate(final Clause originalRoot) {

        // don't rotate any query tree consisting of more than ten clauses. this will disable analysis!
        if(new Counter().getTermCount(originalRoot) < 10){

            // find forests (subtrees) of AndClauses and OrClauses.
            // TODO handle forests hidden behind SingleOperatorClauses (NOT and ANDNO)
            //  although queries rarely start with such clauses.
            // XXX This implementation only handles forests that exist down the right branch only.
            if(originalRoot instanceof BinaryClause){

                LOG.debug(DEBUG_STARTING_ROTATIONS);
                BinaryClause root = (BinaryClause) originalRoot;

                final List<BinaryClause> forestRoots = new ForestFinder().findForestRoots(root);
                LOG.debug(DEBUG_FOUND_FORESTS + forestRoots.size());

                for(BinaryClause clause : forestRoots){

                    final LinkedList<? extends BinaryClause> rotations = createForestRotation(clause);

                    final XorClause result = createXorClause(rotations);
                    // search in root for all occurances of clause and 'replaceDescendant' on each.
                    if(root == clause){
                        root = result;
                    }else{
                        // search in root for all occurances of clause and 'replaceDescendant' on each.
                        final List<BinaryClause> parents = parents(root, clause);
                        for(BinaryClause clauseParent : parents){
                            root = (BinaryClause)replaceDescendant(root, result, clause, clauseParent);
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
        }
        return originalRoot;
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /** {@inherit} **/
    @Override
    protected <T extends UnaryClause> T createOperatorClause(
            final Clause left,
            final Clause right,
            final T replacementFor) {

        LOG.debug("createOperatorClause(" + left + ", " + right + ", " + replacementFor + ")");

        final T clause = super.createOperatorClause(left, right, replacementFor);

        // update our mappings between rotations
        if(replacementFor instanceof BinaryClause && clause instanceof BinaryClause){
            final BinaryClause rf = (BinaryClause)replacementFor;
            final BinaryClause c = (BinaryClause)clause;
            originalFromNew.put(c, rf);
            newFromOriginal.put(rf, c);
            beforeRotationFromNew.put(c, beforeRotationFromOriginal.get(rf));
        }
        return clause;
    }

    /** {@inherit} **/
    @Override
    protected Hint getAlternationHint() {
        return Hint.ROTATION_ALTERNATION;
    }

    // Private -------------------------------------------------------

    private <T extends BinaryClause> LinkedList<T> createForestRotation(
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
            for (Entry<BinaryClause,BinaryClause> entry : beforeRotationFromNew.entrySet()) {

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


    private <T extends BinaryClause> T rotate(
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
            nOrphan = replaceDescendant(rForestRoot, (BinaryClause) nOrphan, rTop, parent(rForestRoot,rTop)); // XXX last argument needs to be from orginal branch
        }
        return (T) nOrphan;
    }


    // Inner classes -------------------------------------------------



}
