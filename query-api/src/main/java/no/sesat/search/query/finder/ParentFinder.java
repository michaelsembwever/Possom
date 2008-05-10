/* Copyright (2007) Schibsted Søk AS
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
 */
package no.sesat.search.query.finder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.parser.AbstractReflectionVisitor;
import no.sesat.search.query.token.TokenPredicate;
import org.apache.log4j.Logger;


/** Visitor used to find a clause's parents.
 * Clauses' do not keep references to their parents as they are immutable and can thus be reused within different trees.
 *
 *
 * @version $Id$
 */
public final class ParentFinder extends AbstractReflectionVisitor implements Serializable {

    private boolean searching = false;
    private boolean singleMode = false;
    private List<OperationClause> parents = new ArrayList<OperationClause>();
    private Clause child;
    private final Map<Clause, Map<Clause, List<OperationClause>>> cache
            = new HashMap<Clause, Map<Clause, List<OperationClause>>>();


    private static final Logger LOG = Logger.getLogger(ParentFinder.class);
    private static final String ERR_CANNOT_CALL_VISIT_DIRECTLY
            = "visit(object) can't be called directly on this visitor!";
    private static final String ERR_CHILD_NOT_IN_HEIRARCHY = "The child is not part of this clause family!";

    private final List<Clause> visitStack = new ArrayList<Clause>();


    /**
     *
     * @param parents
     * @param token
     * @return
     */
    public static boolean insideOf(final List<OperationClause> parents, final TokenPredicate token){

        boolean inside = false;
        for(OperationClause oc : parents){
            inside |= oc.getKnownPredicates().contains(token);
        }
        return inside;
    }

    /** Returns all parents, grandparents, great-grandparents, etc.
     *
     * @param root
     * @param clause
     * @return
     */
    public synchronized List<OperationClause> getAncestors(final Clause root, final Clause clause){

        final List<OperationClause> parents = new ArrayList<OperationClause>();

        for(OperationClause oc : getParents(root, clause)){
            parents.addAll(getAncestors(root, oc));
            parents.add(oc);
        }
        return parents;
    }

    /** Returns all direct parents.
     *
     * @param root
     * @param child
     * @return
     */
    public synchronized List<OperationClause> getParents(final Clause root, final Clause child) {
        findParentsImpl(root, child);
        return Collections.unmodifiableList(new ArrayList<OperationClause>( parents ));
    }

    /** Finds the first found direct parent.
     *
     * @param root
     * @param child
     * @return
     */
    public synchronized OperationClause getParent(final Clause root, final Clause child) {

        singleMode = true;
        findParentsImpl(root, child);
        singleMode = false;
        if (parents.size() == 0) {
            throw new IllegalArgumentException(ERR_CHILD_NOT_IN_HEIRARCHY);
        }
        return parents.get(0);
    }

    private List<OperationClause> findInCache(final Clause root){

        Map<Clause, List<OperationClause>> innerCache = cache.get(root);
        if (innerCache == null){
            innerCache = new HashMap<Clause, List<OperationClause>>();
            cache.put(root, innerCache);
        }
        return innerCache.get(child);
    }

    private void updateCache(final Clause root){

        Map<Clause, List<OperationClause>> innerCache = cache.get(root);
        if (innerCache == null){
            innerCache = new HashMap<Clause, List<OperationClause>>();
            cache.put(root, innerCache);
        }
        innerCache.put(child, new ArrayList<OperationClause>(parents));
    }

    private synchronized <T extends DoubleOperatorClause> void findParentsImpl(final Clause root, final Clause child) {

        this.child = child;
        if (searching || child == null) {
            throw new IllegalStateException(ERR_CANNOT_CALL_VISIT_DIRECTLY);
        }
        searching = true;
        parents.clear();
        visitStack.clear();
        addVisit(root);
        if(null == findInCache(root)){
            visit(root);
            updateCache(root);
        }else{
            parents.addAll(findInCache(root));
        }
        searching = false;
        this.child = null;
    }

    /**
     *
     * @param clause
     */
    protected void visitImpl(final OperationClause clause) {

        if (!singleMode || parents.size() == 0) {

            if (clause.getFirstClause() == child){
                parents.add(clause);
            }

            addVisit(clause.getFirstClause());
            clause.getFirstClause().accept(this);
            removeVisit(clause.getFirstClause());
        }
    }

    /**
     *
     * @param clause
     */
    protected void visitImpl(final DoubleOperatorClause clause) {

        if (!singleMode || parents.size() == 0) {

            if (clause.getFirstClause() == child || clause.getSecondClause() == child) {
                parents.add(clause);
            }

            addVisit(clause.getFirstClause());
            clause.getFirstClause().accept(this);
            removeVisit(clause.getFirstClause());
            addVisit(clause.getSecondClause());
            clause.getSecondClause().accept(this);
            removeVisit(clause.getSecondClause());
        }
    }

    /**
     *
     * @param clause
     */
    protected void visitImpl(final LeafClause clause) {
        // leaves can't be parents :-)
    }


    private void addVisit(final Clause clause){

        if(visitStack.contains(clause)){
            // !serious error! we've gotten into a recursive loop! See SEARCH-2235
            final String msg = "!serious error! we've gotten into a recursive loop! See SEARCH-2235\n";
            final StringBuilder builder = new StringBuilder(msg);

            builder.append("Were looking for child " + child + '\n');

            for(Clause c: visitStack){
                builder.append(c.toString() + '\n');
            }
            builder.append(clause.toString() + '\n');

            LOG.error(builder.toString());
            throw new IllegalStateException(msg);
        }

        visitStack.add(clause);
    }

    private void removeVisit(final Clause clause){

        visitStack.remove(clause);
    }
}