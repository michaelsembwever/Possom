/* Copyright (2006-2007) Schibsted Søk AS
 * WhoWhereSplitter.java
 *
 * Created on 22 February 2007, 14:04
 *
 */

package no.schibstedsok.searchportal.query.finder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.QueryContext;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.token.TokenPredicate;

/** Essentially a QueryTransformer, the similarity is also evident in the context required.
 * <br/>
 * But because it splits the one query into two it doesn't fit into the
 * command's query transformation chain, and is used by commands manually instead.
 * <br/>
 * This class splits the query provided in the context into a who and where components.
 * These are returned as strings in the WhoWhereSplit class and the user is expected to parse each into separate
 *  query objects if required.
 * <br/>
 * The specifications of the split are complicated and were originally given by sesam.se's HittaSearchCommand.
 * <br/>
 * If a query contains multiple fullnames and/or companynames then both who and where will be returned blank.<br/>
 * If just one fullname or companyname is found none of it's terms are moved to the where component.<br/>
 * Otherwise terms which match a geological tokenPredicate and do not match a name or phoneNumber tokenPredicate
 * are moved to the where component.<br/>
 * 
 * <br/>
 * It is guaranteed that all terms in the original query can be found in either the who or where components except
 *  the case when both components are blank.<br/>
 * 
 * <br/>
 * The usefulness of this class is heavy dependant on the fast lists:
 *  FULLNAME, COMPANYNAME, FIRSTNAME, LASTNAME, GEOGLOBAL, and GEOLOCAL;
 * being kept uptodate.<br/>
 * 
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class WhoWhereSplitter extends AbstractReflectionVisitor{
    
    public interface Context extends QueryContext{
        /** Get the terms with their current transformed representations. **/
        Map<Clause,String> getTransformedTerms();
    }
    
    // Constants -----------------------------------------------------
    
    
    // Attributes ----------------------------------------------------
    
    private final Context context;

    private StringBuilder who;
    private StringBuilder where;

    private boolean hasCompany = false;
    private boolean hasFullname = false;
    private boolean multipleCompany = false;
    private boolean multipleFullname = false;
    private boolean validQuery = true;
    private FullnameOrCompanyFinder fullnameOrCompanyFinder = new FullnameOrCompanyFinder();

    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of WhoWhereSplitter */
    public WhoWhereSplitter(final Context context) {
        this.context = context;
    }
    
    // Public --------------------------------------------------------
    
    public WhoWhereSplit getWhoWhereSplit(){

        if(where == null){
            who = new StringBuilder();
            where = new StringBuilder();
            fullnameOrCompanyFinder.visit(context.getQuery().getRootClause());
            if(!(hasCompany && hasFullname) && !multipleCompany && !multipleFullname){
                visit(context.getQuery().getRootClause());
            }
        }

        return new WhoWhereSplit(
            validQuery ? who.toString().trim() : "",
            where.toString().trim()
        );

    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------

    protected void visitImpl(final LeafClause clause) {

        final List<OperationClause> parents  = parentsOf(clause);

        boolean geo = clause.getKnownPredicates().contains(TokenPredicate.GEOLOCAL)
                || clause.getKnownPredicates().contains(TokenPredicate.GEOGLOBAL);

        boolean onlyGeo = geo && clause.getField() == null;

        // check if any possible parents of this clause match the fullname predicate.
        final boolean insideFullname = insideOf(parents, TokenPredicate.FULLNAME);

        boolean isNameOrNumber = clause.getKnownPredicates().contains(TokenPredicate.FIRSTNAME);
        isNameOrNumber |= clause.getKnownPredicates().contains(TokenPredicate.LASTNAME);
        isNameOrNumber |= clause.getKnownPredicates().contains(TokenPredicate.PHONENUMBER);

        // check if any possible parents of this clause match the company predicate.
        final boolean insideCompany = insideOf(parents, TokenPredicate.COMPANYENRICHMENT);

        if(hasCompany || hasFullname){

            onlyGeo &= !insideFullname && !insideCompany;

        }else{

            // no fullname or company exists in the query, so firstname or lastname will do
            onlyGeo &= !isNameOrNumber;
        }

        if (onlyGeo) {
            // add this term to the geo query string
            where.append(context.getTransformedTerms().get(clause));

        }else{
            if((hasCompany && !insideCompany && isNameOrNumber) || multipleCompany || multipleFullname ){
                // this is a company query but this clause isn't the company but a loose name.
                // abort this hitta search, see SEARCH-966 - hitta enrichment
                // OR there are multiple fullnames or company names.
                validQuery = false;
            }else{
                who.append(context.getTransformedTerms().get(clause));
            }
        }
    }

    protected void visitImpl(final OperationClause clause) {
        if(validQuery){
            clause.getFirstClause().accept(this);
        }
    }

    protected void visitImpl(final DoubleOperatorClause clause) {

        if(validQuery){
            clause.getFirstClause().accept(this);
            where.append(' ');
            who.append(' ');
            clause.getSecondClause().accept(this);
        }
    }

    protected void visitImpl(final NotClause clause) {
    }

    protected void visitImpl(final AndNotClause clause) {
    }

    protected void visitImpl(final XorClause clause) {

        switch(clause.getHint()){

            case NUMBER_GROUP_ON_LEFT:
                clause.getSecondClause().accept(this);
                break;

            case PHONE_NUMBER_ON_LEFT:
                if( !clause.getFirstClause().getKnownPredicates().contains(TokenPredicate.PHONENUMBER) ){
                    clause.getSecondClause().accept(this);
                }
                // intentionally fall through to default!
            default:
                clause.getFirstClause().accept(this);
                break;
        }

    }

    // Private -------------------------------------------------------
    
    private List<OperationClause> parentsOf(Clause clause){

        final Query query = context.getQuery();

        final List<OperationClause> parents = new ArrayList<OperationClause>();

        for(OperationClause oc : query.getParentFinder().getParents(query.getRootClause(), clause)){
            parents.add(oc);
            parents.addAll(parentsOf(oc));
        }
        return parents;
    }

    private boolean insideOf(final List<OperationClause> parents, final TokenPredicate token){

        boolean inside = false;
        for(OperationClause oc : parents){
            inside |= oc.getKnownPredicates().contains(token);
        }
        return inside;
    }
    
        
    // Inner classes -------------------------------------------------
        
    private final class FullnameOrCompanyFinder extends AbstractReflectionVisitor{

        protected void visitImpl(final LeafClause clause) {

            final Set<TokenPredicate> predicates = clause.getKnownPredicates();

            final boolean insideFullname = insideOf(parentsOf(clause), TokenPredicate.FULLNAME);

            if(!insideFullname){
                final boolean company = predicates.contains(TokenPredicate.COMPANYENRICHMENT);
                multipleCompany = hasCompany && company;
                hasCompany |= company;
            }
        }

        protected void visitImpl(final OperationClause clause) {

            if(!(hasCompany && hasFullname) && !multipleCompany && !multipleFullname ){
                clause.getFirstClause().accept(this);
            }
        }

        protected void visitImpl(final DoubleOperatorClause clause) {

            if(!(hasCompany && hasFullname) && !multipleCompany && !multipleFullname){
                clause.getFirstClause().accept(this);
                clause.getSecondClause().accept(this);
            }
        }

        protected void visitImpl(final DefaultOperatorClause clause) {

            final List<OperationClause> parents = parentsOf(clause);
            final boolean insideFullname = insideOf(parents, TokenPredicate.FULLNAME);
            final boolean insideCompany = insideOf(parents, TokenPredicate.COMPANYENRICHMENT);

            if(!insideFullname && !insideCompany){
                final Set<TokenPredicate> predicates = clause.getKnownPredicates();

                boolean fullname = predicates.contains(TokenPredicate.FULLNAME);
                multipleFullname = fullname && hasFullname;
                hasFullname |= fullname;

                hasCompany |= !fullname
                    && predicates.contains(TokenPredicate.COMPANYENRICHMENT);

                if(!fullname || !(hasCompany && hasFullname) && !multipleCompany && !multipleFullname){
                    clause.getFirstClause().accept(this);
                    clause.getSecondClause().accept(this);
                }
            }
        }
        protected void visitImpl(final NotClause clause) {
        }

        protected void visitImpl(final AndNotClause clause) {
        }

        protected void visitImpl(final XorClause clause) {
            if(!(hasCompany && hasFullname)){
                clause.getFirstClause().accept(this);
            }
        }
    }
    
    public static final class WhoWhereSplit{
        private final String who;
        private final String where;
        private WhoWhereSplit(final String who, final String where){
            this.who = who;
            this.where = where;
        }
        public String getWho(){
            return who;
        }
        public String getWhere(){
            return where;
        }
    }

}