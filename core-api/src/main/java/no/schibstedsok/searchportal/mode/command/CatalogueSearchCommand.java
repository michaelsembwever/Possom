/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.command.AbstractSearchCommand.ReconstructedQuery;
import no.schibstedsok.searchportal.mode.config.CatalogueSearchConfiguration;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter;
import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter.WhoWhereSplit;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;

import org.apache.log4j.Logger;

/**
 *
 */
public class CatalogueSearchCommand extends AdvancedFastSearchCommand {
    
    private static final Logger LOG = Logger
            .getLogger(CatalogueSearchCommand.class);
    
    private String queryTwo = null;
    private String userSortBy = "kw"; // defualtsøket er på keyword
    private boolean split = false;
    private List knownGeo;
    private String extraGeo;
    
    private static final String DEBUG_CONF_NFO    = "CatalogueSearchCommand Conf details->";
    private static final String DEBUG_SEARCHING_1 = "Catalogue Searching for who->";
    private static final String DEBUG_SEARCHING_2 = "Catalogue Searching for where->";
    private static final String DEBUG_SEARCHING_3 = "Catalogue Searching for geo->";
    
    /**
     * Creates a new catalogue search command.
     *
     */
    public CatalogueSearchCommand(final Context cxt, final DataModel datamodel) {
        super(cxt, datamodel);
        
        final CatalogueSearchConfiguration conf = (CatalogueSearchConfiguration) cxt
                .getSearchConfiguration();
        
        LOG.debug(DEBUG_CONF_NFO + conf.getSearchBy() + ' '
                + conf.getQueryParameterWhere() + ' '
                + conf.getSplit());
        
        // skal query splittes på hva og hvor?
        split = conf.getSplit();
        
        

        
        // split query hvis kommandoen skal dele hva og hvor og flytte geo.
        if(split){
            LOG.info("Vi skal splitte query på HVA og HVOR");
            
            final WhoWhereSplitter splitter = new WhoWhereSplitter(
                    ContextWrapper.wrap(WhoWhereSplitter.Context.class,
                    context,
                    new BaseContext(){
                
                public Map<Clause,String> getTransformedTerms(){
                    return CatalogueSearchCommand.this.getTransformedTerms();
                }
                
                public Query getQuery() {
                    return datamodel.getQuery().getQuery();
                }
            }));
            
            final WhoWhereSplit splitQuery = splitter.getWhoWhereSplit();
            
            getParameters().put("catalogueWhat", splitQuery.getWho());
            getParameters().put("catalogueWhere", splitQuery.getWhere());
            
            
            String[] where = splitQuery.getWhere().split(" ");
            List w = Arrays.asList(where);
            
            LOG.debug(DEBUG_SEARCHING_1 + splitQuery.getWho());
            LOG.debug(DEBUG_SEARCHING_2 + splitQuery.getWhere());
            
            knownGeo=w;
            extraGeo = splitQuery.getWhere();
        }

        // hvis "where" parametern er sendt inn, så tar vi og leser inn query
        // fra den
        ReconstructedQuery rq = null;
        if (getSingleParameter(conf.getQueryParameterWhere()) != null) {
            String tmp = getSingleParameter(conf.getQueryParameterWhere());
            if(extraGeo!=null) tmp = tmp + " " + extraGeo;
            
            rq = createQuery(tmp);
            
            GeoVisitor geo = new GeoVisitor();
            geo.visit(rq.getQuery().getRootClause());
            
            queryTwo = geo.getQueryRepresentation();
            LOG.info(DEBUG_SEARCHING_3 + queryTwo);
        }
        
        
        
        if (getSingleParameter("userSortBy") != null
                && getSingleParameter("userSortBy").length() > 0
                && getSingleParameter("userSortBy").equals("name")) {
            userSortBy = "name";
        } else {
            userSortBy = "kw";
        }        
    }
    
    
    /** TODO comment me. * */
    public SearchResult execute() {
        

        // kjør søk
        SearchResult result = super.execute();
         
        // konverter til denne.
        List<CatalogueSearchResultItem> nyResultListe = new ArrayList<CatalogueSearchResultItem>();
        
        // TODO: get all keys to lookup and execute one call instead of
        // iterating like this...
        Iterator iter = result.getResults().listIterator();
        
        while (iter.hasNext()) {
            BasicSearchResultItem basicResultItem = (BasicSearchResultItem) iter
                    .next();
            
            CatalogueSearchResultItem resultItem = new CatalogueSearchResultItem();
            for (Object o : basicResultItem.getFieldNames()) {
                String s = (String) o;
                String v = basicResultItem.getField(s);
                resultItem.addField(s, v);
            }
            
            nyResultListe.add(resultItem);
        }
        
        // fjern de gamle BasicResultItems, og erstatt dem med nye
        // CatalogueResultItems.
        result.getResults().clear();
        result.getResults().addAll(nyResultListe);
        
        return result;
    }
    
    @Override
    public String getTransformedQuery() {
        String query = super.getTransformedQuery();
        
        if(query.equals("*") && (queryTwo==null || queryTwo.length()==0)){
            return "";
            
        }else if (queryTwo != null && queryTwo.length() > 0 && !query.equals("*")) {
            query += ") " + QL_AND + " (" + queryTwo + ")";
            query = "(" + query;
            
        } else if (query.equals("*")) {
            query = queryTwo;
        }
        
        return query;
    }
    
    
    
    @Override
    protected String getSortBy() {
        // hvis man søker etter firmanavn, sorterer vi etter "iyprpnavn"
        // ellers søker vi etter keywords, og da sorterer vi etter "iyprpkw"
        // istedet.
        String sortBy = "iyprpkw";
        if ("name".equalsIgnoreCase(userSortBy)
        || "exact".equalsIgnoreCase(userSortBy)) {
            sortBy = "iyprpnavn";
        }
        return sortBy;
    }
    
    
    /** TODO comment me. * */
    protected void visitImpl(final LeafClause clause) {
        boolean useTerm = true;
        
        if(split && knownGeo.contains(clause.getTerm())){
            useTerm=false;
        }
        
        if(useTerm){
            if (!getTransformedTerms().get(clause).equals("*")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(");
                    sb.append("iypcfphnavn:" + getTransformedTerms().get(clause)
                                    + " ANY ");
                    sb.append("lemiypcfkeywords:" + getTransformedTerms().get(clause)
                                    + " ANY ");
                    sb.append("lemiypcfkeywordslow:"
                                    + getTransformedTerms().get(clause));
                    sb.append(")");
                    appendToQueryRepresentation(sb.toString());
            }
        }
    }

    /**
     * Legg til iypcfnavn forran alle ord.
     * 
     */
    protected void visitImpl(final PhraseClause clause) {
        boolean useTerm = true;
        
        if(split && knownGeo.contains(clause.getTerm())){
            useTerm=false;
        }
        
        if(useTerm){
            if (!getTransformedTerms().get(clause).equals("*")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(");
                    sb.append("iypcfnavn:" + getTransformedTerms().get(clause)
                                    + " ANY ");
                    sb.append("lemiypcfkeywords:" + getTransformedTerms().get(clause)
                                    + " ANY ");
                    sb.append("lemiypcfkeywordslow:"
                                    + getTransformedTerms().get(clause));
                    sb.append(")");
                    appendToQueryRepresentation(sb.toString());
            }
        }
    }

    
    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final DefaultOperatorClause clause) {

        clause.getFirstClause().accept(this);
        
        final boolean hasKnownGeo = isKnownGeo(clause.getFirstClause()) || isKnownGeo(clause.getSecondClause());
        
        if (!(hasKnownGeo || clause.getSecondClause() instanceof NotClause)) {
            appendToQueryRepresentation(QL_AND);
        }

        clause.getSecondClause().accept(this);
    }    
    
    
    /**
     * Returns true iff the clause is a leaf clause and if it will not produce any output in the query representation.
     *
     * @param clause The clause to examine.
     *
     * @return true iff leaf is empty.
     */
    private boolean isKnownGeo(final Clause clause) {
        if(knownGeo==null) return false;
        
        if (clause instanceof LeafClause) {
            final LeafClause leafClause = (LeafClause) clause;
            return knownGeo.contains(getTransformedTerm(clause));
        } else {
            return false;
        }
    }
    
    

    /**
     * Query builder for creating a query syntax similar to sesam's own.
     */
    private final class GeoVisitor extends AbstractReflectionVisitor {
        
        // AbstractReflectionVisitor overrides
        // ----------------------------------------------
        private final StringBuilder sb = new StringBuilder();
        
        /**
         * Returns the generated query.
         *
         * @return The query.
         */
        String getQueryRepresentation() {
            return sb.toString();
        }
        
        
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final LeafClause clause) {
            if (clause.getTerm() != null && clause.getTerm().length() > 0) {
                sb.append("iypcfgeo:" + clause.getTerm());
            }
        }
        
        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            if (!(clause.getSecondClause() instanceof NotClause)) {
                sb.append(QL_AND);
            }
            clause.getSecondClause().accept(this);
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final OrClause clause) {
            sb.append("(");
            clause.getFirstClause().accept(this);
            
            sb.append(QL_OR);
            
            clause.getSecondClause().accept(this);
            sb.append(")");
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            if (!(clause.getSecondClause() instanceof NotClause)) {
                sb.append(QL_AND);
            }
            clause.getSecondClause().accept(this);
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final NotClause clause) {
            
            final String childsTerm = clause.getFirstClause().getTerm();
            if (childsTerm != null && childsTerm.length() > 0) {
                sb.append(QL_ANDNOT);
                clause.getFirstClause().accept(this);
            }
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final AndNotClause clause) {
            // the first term can not be ANDNOT term.
            if(sb.toString().trim().length()>0){
                sb.append(QL_ANDNOT);
                clause.getFirstClause().accept(this);
            }
        }
        
        protected void visitImpl(final XorClause clause){
            clause.getFirstClause().accept(this);
        }
    }
    
    

    
    /**
     * Remove geographic places if present in what, if present in where.
     */
    private final class GeoZapperVisitor extends AbstractReflectionVisitor {
        
        List knownGeo;
        
        GeoZapperVisitor(List knownGeo){
            this.knownGeo = knownGeo;
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final LeafClause clause) {
            
            LOG.info("Split: "+split);
            LOG.info("KnownGeo: "+knownGeo);
            LOG.info("Clause: "+clause);
            LOG.info("Term: "+clause.getTerm());
            CatalogueSearchCommand.this.getTransformedTerms().put(clause,"");
        }

        
        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final OrClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final NotClause clause) {
            final String childsTerm = clause.getFirstClause().getTerm();
            if (childsTerm != null && childsTerm.length() > 0) {
                clause.getFirstClause().accept(this);
            }
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final AndNotClause clause) {
            clause.getFirstClause().accept(this);
        }
        
        protected void visitImpl(final XorClause clause){
            clause.getFirstClause().accept(this);
        }        
    }
        
    
    
}
