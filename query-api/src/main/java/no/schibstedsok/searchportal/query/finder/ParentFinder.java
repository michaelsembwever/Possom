// Copyright (2007) Schibsted Søk AS
// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;


// Inner classes -------------------------------------------------

public final class ParentFinder extends AbstractReflectionVisitor {

    private boolean searching = false;
    private boolean singleMode = false;
    private List<OperationClause> parents = new ArrayList<OperationClause>();
    private Clause child;
    private final Map<Clause, Map<Clause, List<OperationClause>>> cache 
            = new HashMap<Clause, Map<Clause, List<OperationClause>>>();

    private static final String ERR_CANNOT_CALL_VISIT_DIRECTLY 
            = "visit(object) can't be called directly on this visitor!";
    private static final String ERR_CHILD_NOT_IN_HEIRARCHY = "The child is not part of this clause family!";


    public synchronized List<OperationClause> getParents(final Clause root, final Clause child) {
        findParentsImpl(root, child);
        return Collections.unmodifiableList(new ArrayList<OperationClause>( parents ));
    }

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
        if(null == findInCache(root)){
            visit(root);
            updateCache(root);
        }else{
            parents.addAll(findInCache(root));
        }
        searching = false;
        this.child = null;
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