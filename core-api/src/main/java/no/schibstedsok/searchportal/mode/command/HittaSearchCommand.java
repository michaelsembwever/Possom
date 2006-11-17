// Copyright (2006) Schibsted Søk AS
/*
 * HittaSearchCommand.java
 *
 * Created on May 30, 2006, 3:27 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.rpc.ServiceException;
import no.schibstedsok.searchportal.mode.config.HittaSearchConfiguration;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.view.spell.QuerySuggestion;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;
import org.apache.log4j.Logger;
import se.hitta.www.HittaService.HittaServiceLocator;
import se.hitta.www.HittaService.HittaServiceSoap;

/** Search against the Swedish Hitta WebService. ✆
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class HittaSearchCommand extends AbstractWebServiceSearchCommand{


    // Constants -----------------------------------------------------

    private static final Collection<TokenPredicate> WHO_PREDICATES = Collections.unmodifiableCollection(
            Arrays.asList(
                TokenPredicate.COMPANYENRICHMENT,
                TokenPredicate.FIRSTNAME,
                TokenPredicate.LASTNAME,
                TokenPredicate.PHONENUMBER
            ));

    private static final Logger LOG = Logger.getLogger(HittaSearchCommand.class);
    private static final String ERR_FAILED_HITTA_SEARCH = "Failed Hitta search command";
    private static final String DEBUG_CONF_NFO = "Conf details --> ";
    private static final String DEBUG_SEARCHING_1 = "Searching for who->";
    private static final String DEBUG_SEARCHING_2 = "Searching for where->";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of HittaSearchCommand.
     */
    public HittaSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);

    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    /** @inherit **/
    public SearchResult execute(){

        final HittaSearchConfiguration conf = (HittaSearchConfiguration) context.getSearchConfiguration();
        int hits = 0;

        LOG.debug(DEBUG_CONF_NFO + conf.getCatalog() + ' ' + conf.getKey());

        if(getTransformedQuery().equals(untransformedQuery)){

            try {

                final HittaServiceLocator locator = new HittaServiceLocator();
                final HittaServiceSoap service = locator.getHittaServiceSoap();
                final SplitQueryTransformer splitter = new SplitQueryTransformer();

                final String[] splitQuery = splitter.getQuery();

                if( splitQuery[0].length() >0 ){

                    getParameters().put("hittaWho", splitQuery[0]);
                    getParameters().put("hittaWhere", splitQuery[1]);

                    LOG.debug(DEBUG_SEARCHING_1 + splitQuery[0]);
                    LOG.debug(DEBUG_SEARCHING_2 + splitQuery[1]);

                    if(conf.getCatalog().equalsIgnoreCase("white")){
                        hits = service.getWhiteAmount(splitQuery[0], splitQuery[1], conf.getKey());

                    }else if(conf.getCatalog().equalsIgnoreCase("pink")){
                        hits = service.getPinkAmount(splitQuery[0], splitQuery[1], conf.getKey());

                    }
                }
            } catch (ServiceException ex) {
                LOG.error(ERR_FAILED_HITTA_SEARCH, ex);
            } catch (RemoteException ex) {
                LOG.error(ERR_FAILED_HITTA_SEARCH, ex);
            }
        }

        

        final SearchResult result = new WebServiceSearchResult(this);
        result.setHitCount(hits);


        return result;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------




    // Inner classes -------------------------------------------------

    private static final class WebServiceSearchResult extends BasicSearchResult{

        private static final String ERR_NOT_SUPPORTED = "Not part of this implementation";

        public WebServiceSearchResult(final SearchCommand command) {
            super(command);
        }

        public void addResult(final SearchResultItem item) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public void addSpellingSuggestion(final SpellingSuggestion suggestion) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public Map<String, List<SpellingSuggestion>> getSpellingSuggestions() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public List<QuerySuggestion> getQuerySuggestions() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public void addQuerySuggestion(final QuerySuggestion query) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public List<SearchResultItem> getResults() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

    }

    private final class SplitQueryTransformer extends AbstractReflectionVisitor{

        private StringBuilder who;
        private StringBuilder where;

        private boolean hasCompany = false;
        private boolean hasFullname = false;
        private boolean multipleCompany = false;
        private boolean multipleFullname = false;
        private boolean validQuery = true;
        private FullnameOrCompanyFinder fullnameOrCompanyFinder = new FullnameOrCompanyFinder();

        public String[] getQuery(){

            if(where == null){
                who = new StringBuilder();
                where = new StringBuilder();
                fullnameOrCompanyFinder.visit(context.getQuery().getRootClause());
                if(!(hasCompany && hasFullname) && !multipleCompany && !multipleFullname){
                        visit(context.getQuery().getRootClause());
                }
            }

            return new String[]{
                validQuery ? who.toString().trim() : "",
                where.toString().trim()
            };
              
        }
        
        private List<OperationClause> parentsOf(Clause clause){
            
            final Query query = context.getQuery();
            
            final List<OperationClause> parents = new ArrayList<OperationClause>();
            
            for(OperationClause oc : query.getParentFinder().getParents(query.getRootClause(), clause)){
                parents.add(oc);
                parents.addAll(parentsOf(oc));
            }
            return parents;
        }

        protected void visitImpl(final LeafClause clause) {
                
            final List<OperationClause> parents  = parentsOf(clause);

            boolean geo = clause.getKnownPredicates().contains(TokenPredicate.GEOLOCAL)
                    || clause.getKnownPredicates().contains(TokenPredicate.GEOGLOBAL);
            
            boolean onlyGeo = geo && clause.getField() == null;
            
            // check if any possible parents of this clause match the fullname predicate.
            boolean insideFullname = false;
            for(OperationClause oc : parents){
                insideFullname |= oc.getKnownPredicates().contains(TokenPredicate.FULLNAME);
            }
            
            boolean isNameOrNumber = clause.getKnownPredicates().contains(TokenPredicate.FIRSTNAME);
            isNameOrNumber |= clause.getKnownPredicates().contains(TokenPredicate.LASTNAME);
            isNameOrNumber |= clause.getKnownPredicates().contains(TokenPredicate.PHONENUMBER);
            
            // check if any possible parents of this clause match the company predicate.
            boolean isCompany = false;
            for(OperationClause oc : parents){
                isCompany |= oc.getKnownPredicates().contains(TokenPredicate.COMPANYENRICHMENT);
            }

            if(hasCompany || hasFullname){

                onlyGeo &= !insideFullname && !isCompany;

            }else{
                
                // no fullname or company exists in the query, so firstname or lastname will do
                onlyGeo &= !isNameOrNumber;
            }

            if (onlyGeo) {
                // add this term to the geo query string
                where.append(getTransformedTerm(clause));
                
            }else{
                if((hasCompany && !isCompany && isNameOrNumber) || multipleCompany || multipleFullname ){
                    // this is a company query but this clause isn't the company but a loose name.
                    // abort this hitta search, see SEARCH-966 - hitta enrichment
                    // OR there are multiple fullnames or company names.
                    validQuery = false;
                }else{
                    who.append(getTransformedTerm(clause));
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

        private final class FullnameOrCompanyFinder extends AbstractReflectionVisitor{

            protected void visitImpl(final LeafClause clause) {

                final Set<TokenPredicate> predicates = clause.getKnownPredicates();

                final boolean company = predicates.contains(TokenPredicate.COMPANYENRICHMENT);
                multipleCompany = hasCompany && company;
                hasCompany |= company;
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
    }

}