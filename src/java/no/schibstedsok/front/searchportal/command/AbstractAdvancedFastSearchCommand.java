/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractAdvancedFastSearchCommand.java
 *
 * Created on 14 March 2006, 19:51
 *
 */

package no.schibstedsok.front.searchportal.command;

import com.fastsearch.esp.search.ConfigurationException;
import com.fastsearch.esp.search.ISearchFactory;
import com.fastsearch.esp.search.SearchEngineException;
import com.fastsearch.esp.search.SearchFactory;
import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.query.Query;
import com.fastsearch.esp.search.query.SearchParameter;
import com.fastsearch.esp.search.result.EmptyValueException;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IModifier;
import com.fastsearch.esp.search.result.INavigator;
import com.fastsearch.esp.search.result.IQueryResult;
import com.fastsearch.esp.search.result.IllegalType;
import com.fastsearch.esp.search.view.ISearchView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.AdvancedFastConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * Base class for commands queryinga FAST EPS Server.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractAdvancedFastSearchCommand extends AbstractSearchCommand {

    // Constants -----------------------------------------------------
    private final static String FACTORY_PROPERTY = 
            "com.fastsearch.esp.search.SearchFactory";
    private final static String HTTP_FACTORY =
            "com.fastsearch.esp.search.http.HttpSearchFactory";
    private final static String QR_SERVER_PROPERTY = 
            "com.fastsearch.esp.search.http.qrservers";
    private final static String ENCODER_PROPERTY = 
            "com.fastsearch.esp.search.http.encoderclass";
    private final static String ENCODER_CLASS = 
            "com.fastsearch.esp.search.http.DSURLUTF8Encoder";
    private final static String COLLAPSE_PARAMETER="collapse";
    
    private static final Logger LOG = 
            Logger.getLogger(AbstractSimpleFastSearchCommand.class);


    // Attributes ----------------------------------------------------
    private final AdvancedFastConfiguration cfg;

    private Map<String,FastNavigator> navigatedTo = new HashMap<String,FastNavigator>();
    private Map<String,String[]> navigatedValues = new HashMap<String,String[]>();
    
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of AbstractAdvancedFastSearchCommand */
    public AbstractAdvancedFastSearchCommand(
                    final Context cxt,
                    final Map parameters) {

        super(cxt, parameters);
          
        cfg = (AdvancedFastConfiguration) getSearchConfiguration();
    }

    // Public --------------------------------------------------------
    public SearchResult execute() {

        final Properties props = new Properties();

        props.setProperty(FACTORY_PROPERTY, HTTP_FACTORY);
        props.setProperty(QR_SERVER_PROPERTY, cfg.getQueryServer());
        props.setProperty(ENCODER_PROPERTY, ENCODER_CLASS);

        try {

            StringBuilder filterBuilder = new StringBuilder();
            
            final ISearchFactory factory = SearchFactory.newInstance(props);

            final String transformedQuery = getTransformedQuery();
            
            LOG.debug("Transformed query is " + transformedQuery);
            
            final String collapseId = getParameter(COLLAPSE_PARAMETER);
            
            final IQuery query = new Query(transformedQuery);

            if (cfg.isCollapsingEnabled()) {
                if (collapseId == null || collapseId.equals("")) {
                    query.setParameter(new SearchParameter(
                            BaseParameter.COLLAPSING, true));

                    if (!cfg.getCollapseOnField().equals("")) {
                        query.setParameter(new SearchParameter(
                                "collapseon", cfg.getCollapseOnField()));
                    }
                } else {
                    filterBuilder.append("+collapseid:").append(collapseId);
                }
            }

            query.setParameter(new SearchParameter(
                    BaseParameter.OFFSET, getCurrentOffset(0)));
            query.setParameter(new SearchParameter(
                    BaseParameter.HITS, cfg.getResultsToReturn()));
            query.setParameter(new SearchParameter(
                    BaseParameter.SORT_BY, cfg.getSortBy()));
            query.setParameter(new SearchParameter(BaseParameter.FILTER, 
                    filterBuilder.toString()));
            
            final ISearchView view = factory.getSearchView(cfg.getView());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Query is " + query);
            }

            final IQueryResult result = view.search(query);
            
            final FastSearchResult searchResult = new FastSearchResult(this);

            final int cnt = getCurrentOffset(0);
            final int maxIndex = getMaxDocIndex(result, cnt, cfg);
            
            searchResult.setHitCount(result.getDocCount());

            for (int i = cnt; i < maxIndex; i++) {
                try {
                    final IDocumentSummary document = result.getDocument(i + 1);
                    searchResult.addResult(createResultItem(document));
                } catch (NullPointerException e) { // THe doc count is not 100% accurate.
                    if (LOG.isDebugEnabled())
                        LOG.debug("Error finding document " + e);
                    return searchResult;
                }
            }

            return searchResult;

        } catch (ConfigurationException ex) {
            LOG.error("exeute ", ex);
            throw new InfrastructureException(ex);
        } catch (SearchEngineException ex) {
            LOG.error("exeute ", ex);
            throw new InfrastructureException(ex);
        } catch (IOException ex) {
            LOG.error("exeute ", ex);
            throw new InfrastructureException(ex);
        }
    }

    private int getMaxDocIndex(
            final IQueryResult result, 
            final int cnt, 
            final AdvancedFastConfiguration cfg) 
    {
        return Math.min(cnt + cfg.getResultsToReturn(), result.getDocCount());
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------
    // Generate query in FQL.
   
   /**
    *
    * @todo Work in progress.
    * quoting reserved words, operator precedence and more. 
    */ 
    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
            appendToQueryRepresentation(getTransformedTerm(clause));
        }
    }
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }
    protected void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" and ");
        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation(" (");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" or ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(") ");
    }
    protected void visitImpl(final DefaultOperatorClause clause) {

        
        
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" and ");
        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final NotClause clause) {
        appendToQueryRepresentation(" not ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");

    }
    protected void visitImpl(final AndNotClause clause) {
        appendToQueryRepresentation("andnot ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");
    }
    /**
     *
     * @param clause The clause to examine.
     */
    protected void visitImpl(final XorClause clause) {
        if (clause.getHint() == XorClause.PHRASE_ON_LEFT) {
            // Web searches should use phrases over separate words.
            clause.getFirstClause().accept(this);
        } else {
            // All other high level clauses are ignored.
            clause.getSecondClause().accept(this);
        }
    }

    protected Map<String,FastNavigator> getNavigators() {
        return cfg.getNavigators();
    }

    
    // Private -------------------------------------------------------

    private SearchResultItem createResultItem(final IDocumentSummary document) {

        final SearchResultItem item = new BasicSearchResultItem();

        for (final String field : cfg.getResultFields()) {

            final String split[] = field.split("AS");
            final String alias = split.length == 2 ? split[0].trim() : field;
            final IDocumentSummaryField summary = document.getSummaryField(field);

            if (summary != null && !summary.isEmpty()) 
                item.addField(alias, summary.getStringValue().trim());
        }

        
        
        if (cfg.isCollapsingEnabled()) {
            final String currCollapseId = getParameter(COLLAPSE_PARAMETER);

            if (currCollapseId == null || currCollapseId.equals("")) {
                final String moreHits = document.getSummaryField("morehits").getStringValue();
                
                if (moreHits.equals("1")) {
                    item.addField("moreHits", "true");
                    item.addField("collapseParameter", COLLAPSE_PARAMETER);
                    item.addField("collapseId", document.getSummaryField("collapseid").getStringValue());
                }
            }
        }
        return item;
    }

    // Inner classes -------------------------------------------------
}

