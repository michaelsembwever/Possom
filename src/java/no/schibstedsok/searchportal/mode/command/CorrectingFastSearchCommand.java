/*
 * CorrectingFastSearchCommand.java
 *
 * Created on August 29, 2006, 1:08 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchType;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.QueryContext;
import no.schibstedsok.searchportal.query.QueryStringContext;
import no.schibstedsok.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.searchportal.query.parser.QueryParser;
import no.schibstedsok.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineImpl;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.util.config.DocumentLoader;
import no.schibstedsok.searchportal.util.config.PropertiesLoader;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;
import org.apache.log4j.Logger;

/**
 *
 * This class can be extended to get the following behaviour.
 *
 * If the execution of the search command does not return any hits and if there
 * are spelling suggestions available, correct the query and rerun the command.
 *
 * @author maek
 */
public abstract class CorrectingFastSearchCommand extends AbstractSimpleFastSearchCommand {
    
    private static final String ERR_CANNOT_CREATE_COMMAND =
            "Unable to create command to rerun.";
    
    private static final Logger LOG = Logger.getLogger(CorrectingFastSearchCommand.class);
    
    private boolean correct = true;

    /**
     * A token evaluation engine for corrected query.
     */
    private TokenEvaluationEngine tokenEvaluationEngine;
    
    /** Creates a new instance of CorrectionFastSearchCommand.
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public CorrectingFastSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }
    
    /**
     * @inherit
     */
    public SearchResult execute() {
        FastSearchResult originalResult = (FastSearchResult) super.execute();
        
        final Map suggestions = originalResult.getSpellingSuggestions();
        
        // Rerun command?
        if (correct && originalResult.getHitCount() == 0 && !suggestions.isEmpty()) {
            // Correct spelling suggestions and parse the resulting query string.
            final String oldQuery = context.getRunningQuery().getQueryString();
            final String newQuery = correctQuery(suggestions, oldQuery);

            final TokenEvaluationEngine engine = createEngineForQuery(newQuery);
            
            final Query queryObj = parseNewQuery(newQuery, engine);
            
            // Create a new identical context apart from the corrected query.
            // @todo use ContextWrapper instead.
            final SearchCommand.Context cmdCxt = new SearchCommand.Context() {
                public PropertiesLoader newPropertiesLoader(
                        final String resource, final Properties properties) {
                    return context.newPropertiesLoader(resource, properties);
                }
                
                public DocumentLoader newDocumentLoader(
                        final String resource, final DocumentBuilder builder) {
                    return context.newDocumentLoader(resource, builder);
                }
                
                public Site getSite() {
                    return context.getSite();
                }
                
                public SearchConfiguration getSearchConfiguration() {
                    return context.getSearchConfiguration();
                }
                
                public RunningQuery getRunningQuery() {
                    return context.getRunningQuery();
                }
                
                public Query getQuery() {
                    return queryObj;
                }
            };
            
            try {
                // Create and execute command on corrected query.
                // Making sure this new command does not try to do the whole
                // correction thing all over again.
                final CorrectingFastSearchCommand c = createCommand(cmdCxt);
                c.performQueryTransformation();
                c.correct = false;
                c.tokenEvaluationEngine = engine;
                
                return c.execute();
            } catch (Exception ex) {
                LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                return originalResult;
            }
        }
        
        return originalResult;
    }
    
    /**
     * Returns a token evaluation engine for the corrected query or null if
     * the query hasn't been corrected. The engine available via RunningQuery
     * has been run against the original query so in order to do programatic
     * predicate checking on corrected query this one must be used.
     *
     * @return TokenEvaluation engine for corrected query.
     */
    protected TokenEvaluationEngine getTokenEvaluationEngine() {
        return tokenEvaluationEngine;
    }

    // TODO comment me.
    protected void setAdditionalParameters(final ISearchParameters params) {
        super.setAdditionalParameters(params);
        params.setParameter(new SearchParameter(BaseParameter.TYPE, SearchType.SEARCH_ADVANCED.getValueString()));
    }

    // Implementation of advanced query language. The spelling suggestions for
    // yellow and white only works as is should when the advanced query language
    // is used.
    protected void visitImpl(final AndClause clause) {
        // The leaf clauses might not produce any output. For example terms 
        // having a site: field. In these cases we should not output the 
        // operator keyword.
        boolean hasEmptyLeaf = false;

        hasEmptyLeaf |= isEmptyLeaf(clause.getFirstClause());
        hasEmptyLeaf |= isEmptyLeaf(clause.getSecondClause());
        
        clause.getFirstClause().accept(this);

        if (! hasEmptyLeaf) 
            appendToQueryRepresentation(" AND ");

        clause.getSecondClause().accept(this);
    }

    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation(" (");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" OR ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(") ");
    }

    protected void visitImpl(final DefaultOperatorClause clause) {
        boolean hasEmptyLeaf = false;

        hasEmptyLeaf |= isEmptyLeaf(clause.getFirstClause());
        hasEmptyLeaf |= isEmptyLeaf(clause.getSecondClause());

        clause.getFirstClause().accept(this);
        
        if (! hasEmptyLeaf)
            appendToQueryRepresentation(" AND ");

        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final NotClause clause) {
        appendToQueryRepresentation(" ANDNOT ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");

    }
    protected void visitImpl(final AndNotClause clause) {
        appendToQueryRepresentation("ANDNOT ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");
    }

    private boolean isEmptyLeaf(final Clause clause) {
        if (clause instanceof LeafClause) {
            final LeafClause leaf = (LeafClause) clause;
            return leaf.getField() != null;
        }

        return false;
    }
    
    private CorrectingFastSearchCommand createCommand(final SearchCommand.Context cmdCxt) throws Exception {
        final Class<? extends CorrectingFastSearchCommand> clazz = getClass();
        final Constructor<? extends CorrectingFastSearchCommand> con
                = clazz.getConstructor(Context.class, Map.class);
        return con.newInstance(cmdCxt, getParameters());
    }
    
    private TokenEvaluationEngine createEngineForQuery(final String q) {
        final TokenEvaluationEngineImpl.Context tokenEvalFactoryCxt =
                ContextWrapper.wrap(
                TokenEvaluationEngineImpl.Context.class,
                context,
                new QueryStringContext() {
            public String getQueryString() {
                return q;
            }
        });
        
        return new TokenEvaluationEngineImpl(tokenEvalFactoryCxt);
    }
    
    private Query parseNewQuery(final String q, final TokenEvaluationEngine e) {
        final QueryParser parser
                = new QueryParserImpl(new AbstractQueryParserContext() {
            public TokenEvaluationEngine getTokenEvaluationEngine() {
                return e;
            }
        });
        
        return parser.getQuery();
    }
    
    private String correctQuery(
            final Map<String, List<SpellingSuggestion>> suggestions,
            final String q) {
        
        String newQ = q;
        
        for (final List<SpellingSuggestion> suggestionList : suggestions.values()) {
            for (final SpellingSuggestion s : suggestionList) {
                newQ = newQ.replaceAll(s.getOriginal(), s.getSuggestion());
            }
        }
        
        final String fixedQueryString = newQ;
        return fixedQueryString;
    }
}
