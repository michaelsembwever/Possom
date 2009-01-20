/* Copyright (2006-2008) Schibsted Søk AS
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
 * WhoWhereSplitter.java
 *
 * Created on 22 February 2007, 14:04
 *
 */

package no.sesat.search.query.finder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.BinaryClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.UnaryClause;
import no.sesat.search.query.QueryContext;
import no.sesat.search.query.XorClause;
import no.sesat.commons.visitor.AbstractReflectionVisitor;
import no.sesat.search.query.token.Categories;
import no.sesat.search.query.token.TokenPredicate;

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
 * The usefulness of this class is heavy dependant on the TokenPredicates:
 *  FULLNAME, COMPANYNAME, COMPANY_KEYWORD, FIRSTNAME, LASTNAME, GEOGLOBAL, and GEOLOCAL;
 * being kept available and uptodate.<br/>
 *
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class WhoWhereSplitter extends AbstractReflectionVisitor{

    /**
     * Context this class requires to work within.
     */
    public interface Context extends QueryContext{
        /** Get the terms with their current transformed representations. *
         * @return
         */
        Map<Clause,String> getTransformedTerms();
        /**
         *
         * @return
         */
        List<Application> getApplications();
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

    private final FullnameOrCompanyFinder fullnameOrCompanyFinder = new FullnameOrCompanyFinder();
    private final Set<UnaryClause> invalidatedPlaces = new HashSet<UnaryClause>();

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of WhoWhereSplitter
     * @param context
     */
    public WhoWhereSplitter(final Context context) {
        this.context = context;
    }

    // Public --------------------------------------------------------

    /**
     *
     * @return
     */
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

    /**
     *
     * @param clause
     */
    protected void visitImpl(final LeafClause clause) {

        final List<UnaryClause> parents
                = context.getQuery().getParentFinder().getAncestors(context.getQuery().getRootClause(), clause);

        final List<UnaryClause> validGeoParents = new ArrayList<UnaryClause>(parents);
        validGeoParents.removeAll(invalidatedPlaces);

        boolean geo = clause.getKnownPredicates().contains(Categories.GEOLOCAL)
                || clause.getKnownPredicates().contains(Categories.GEOGLOBAL)
                || ParentFinder.insideOf(validGeoParents, Categories.GEOLOCAL)
                || ParentFinder.insideOf(validGeoParents, Categories.GEOGLOBAL);

        boolean onlyGeo = geo && clause.getField() == null;

        // check if any possible parents of this clause match the fullname predicate.
        final boolean insideFullname = context.getApplications().contains(Application.WHITE)
                && ParentFinder.insideOf(parents, Categories.FULLNAME);

        boolean isNameOrNumber = context.getApplications().contains(Application.WHITE)
                && clause.getKnownPredicates().contains(Categories.FIRSTNAME);

        isNameOrNumber |= context.getApplications().contains(Application.WHITE)
                && clause.getKnownPredicates().contains(Categories.LASTNAME);

        isNameOrNumber |= clause.getKnownPredicates().contains(Categories.PHONENUMBER);

        // check if the clause or any possible parents of this clause match the company predicate.
        boolean isOrInsideCompany = context.getApplications().contains(Application.YELLOW);
        isOrInsideCompany &=
                clause.getKnownPredicates().contains(Categories.COMPANYENRICHMENT)
                || clause.getKnownPredicates().contains(Categories.COMPANY_KEYWORD)
                || ParentFinder.insideOf(parents, Categories.COMPANYENRICHMENT)
                || ParentFinder.insideOf(parents, Categories.COMPANY_KEYWORD);

        if(hasCompany || hasFullname){

            onlyGeo &= !insideFullname && !isOrInsideCompany;

        }else{

            // no fullname or company exists in the query, so firstname or lastname will do
            onlyGeo &= !isNameOrNumber;
        }

        if (onlyGeo) {
            // add this term to the geo query string
            where.append(context.getTransformedTerms().get(clause));

        }else{
            if((hasCompany && !isOrInsideCompany && isNameOrNumber) || multipleCompany || multipleFullname ){
                // this is a company query but this clause isn't the company but a loose name.
                // abort this hitta search, see SEARCH-966 - hitta enrichment
                // OR there are multiple fullnames or company names.
                validQuery = false;
            }else{

                who.append(context.getTransformedTerms().get(clause));

                // invalidate any parent geo term since part of it has now been used in the who field
                for(UnaryClause parent : parents){

                    if(parent.getKnownPredicates().contains(Categories.GEOLOCAL)
                            || parent.getKnownPredicates().contains(Categories.GEOGLOBAL)){

                        invalidatedPlaces.add(parent);
                    }
                }
            }
        }
    }

    /**
     *
     * @param clause
     */
    protected void visitImpl(final UnaryClause clause) {
        if(validQuery){
            clause.getFirstClause().accept(this);
        }
    }

    /**
     *
     * @param clause
     */
    protected void visitImpl(final BinaryClause clause) {

        if(validQuery){
            clause.getFirstClause().accept(this);
            where.append(' ');
            who.append(' ');
            clause.getSecondClause().accept(this);
        }
    }

    /**
     *
     * @param clause
     */
    protected void visitImpl(final NotClause clause) {
    }

    /**
     *
     * @param clause
     */
    protected void visitImpl(final AndNotClause clause) {
    }

    /**
     *
     * @param clause
     */
    @SuppressWarnings("fallthrough")
    protected void visitImpl(final XorClause clause) {

        switch(clause.getHint()){

            case NUMBER_GROUP_ON_LEFT:
                clause.getSecondClause().accept(this);
                break;

            case PHONE_NUMBER_ON_LEFT:
                if( !clause.getFirstClause().getKnownPredicates().contains(Categories.PHONENUMBER) ){
                    clause.getSecondClause().accept(this);
                }
                // intentionally fall through to default!
            default:
                clause.getFirstClause().accept(this);
                break;
        }

    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

    private final class FullnameOrCompanyFinder extends AbstractReflectionVisitor{

        protected void visitImpl(final LeafClause clause) {

            final Set<TokenPredicate> predicates = clause.getKnownPredicates();

            final boolean insideFullname = context.getApplications().contains(Application.WHITE)
                    && ParentFinder.insideOf(context.getQuery().getParentFinder().getAncestors(
                        context.getQuery().getRootClause(), clause),
                        Categories.FULLNAME);

            if(!insideFullname){
                boolean company = context.getApplications().contains(Application.YELLOW);
                company &= predicates.contains(Categories.COMPANYENRICHMENT)
                        || predicates.contains(Categories.COMPANY_KEYWORD);

                multipleCompany = hasCompany && company;
                hasCompany |= company;
            }
        }

        protected void visitImpl(final UnaryClause clause) {

            if(!(hasCompany && hasFullname) && !multipleCompany && !multipleFullname ){
                clause.getFirstClause().accept(this);
            }
        }

        protected void visitImpl(final BinaryClause clause) {

            if(!(hasCompany && hasFullname) && !multipleCompany && !multipleFullname){
                clause.getFirstClause().accept(this);
                clause.getSecondClause().accept(this);
            }
        }

        protected void visitImpl(final DefaultOperatorClause clause) {

            final List<UnaryClause> parents
                    = context.getQuery().getParentFinder().getAncestors(context.getQuery().getRootClause(), clause);

            final boolean insideFullname = context.getApplications().contains(Application.WHITE)
                    && ParentFinder.insideOf(parents, Categories.FULLNAME);

            boolean insideCompany = context.getApplications().contains(Application.YELLOW);
            insideCompany &= ParentFinder.insideOf(parents, Categories.COMPANYENRICHMENT)
                    || ParentFinder.insideOf(parents, Categories.COMPANY_KEYWORD);

            if(!insideFullname && !insideCompany){
                final Set<TokenPredicate> predicates = clause.getKnownPredicates();

                boolean fullname = context.getApplications().contains(Application.WHITE)
                        && predicates.contains(Categories.FULLNAME);

                multipleFullname = fullname && hasFullname;
                hasFullname |= fullname;

                hasCompany |= !fullname && context.getApplications().contains(Application.YELLOW)
                    && (predicates.contains(Categories.COMPANYENRICHMENT)
                        || predicates.contains(Categories.COMPANY_KEYWORD));

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

    /**
     *
     */
    public static final class WhoWhereSplit{
        private final String who;
        private final String where;
        public WhoWhereSplit(final String who, final String where){

            this.who = who;
            this.where = where;
        }
        /**
         *
         * @return
         */
        public String getWho(){
            return who;
        }
        /**
         *
         * @return
         */
        public String getWhere(){
            return where;
        }
    }

    /**
     *
     */
    public enum Application{
        /**
         * Apply WhoWhereSplitter to white logic.
         *  eg fullname, firstname, and lastname lists.
         */
        WHITE,
        /**
         * Apply WhoWhereSplitter to yellow logic.
         *  eg companyenrich list.
         */
        YELLOW;
    }
}
