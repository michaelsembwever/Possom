// Copyright (2005-2006) Schibsted Søk AS
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

import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchType;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
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
    private static final String RESULT_FIELD_CORRECTED_QUERY =
            "autoCorrectedQuery";

    private static final Logger LOG = Logger.getLogger(CorrectingFastSearchCommand.class);

    private boolean correct = true;

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

        final FastSearchResult originalResult = (FastSearchResult) super.execute();
        final Map suggestions = originalResult.getSpellingSuggestions();

        // Rerun command?
        // TODO Consider moving the isCorrectionEnabled() call after the
        // correction has been made and then discarding the result
        // should the call return false.
        // Sub classes might not know if the corrected query should be used
        // until after the query has been run. or at least not after a token
        // evaluation has been run on the corrected query.
        if (isCorrectionEnabled() && correct && originalResult.getHitCount() == 0 && !suggestions.isEmpty()) {
            // Correct spelling suggestions and parse the resulting query string.
            final String oldQuery = context.getRunningQuery().getQueryString();
            final String newQuery = correctQuery(suggestions, oldQuery);

            // Create a new identical context apart from the corrected query
            final ReconstructedQuery rq = createQuery(newQuery);
            final SearchCommand.Context cmdCxt = ContextWrapper.wrap(
                    SearchCommand.Context.class,
                    new BaseContext(){
                        public Query getQuery() {
                            return rq.getQuery();
                        }
                        public TokenEvaluationEngine getTokenEvaluationEngine(){
                            return rq.getEngine();
                        }
                    },
                    context
                );


            try {
                // Create and execute command on corrected query.
                // Making sure this new command does not try to do the whole
                // correction thing all over again.
                final CorrectingFastSearchCommand c = createCommand(cmdCxt);
                c.performQueryTransformation();
                c.correct = false;

                final SearchResult result = c.execute();

                if (result.getHitCount() > 0) {
                    result.addField(RESULT_FIELD_CORRECTED_QUERY, newQuery);
                }

                return result;

            } catch (Exception ex) {
                LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                return originalResult;
            }
        }

        return originalResult;
    }

    /**
     * Correction will only be enabled if this method returns true. Override
     * this to dynamically turn correction on and off.
     *
     * @return true
     */
    protected boolean isCorrectionEnabled() {
        return true;
    }

    // TODO comment me.
    /** TODO comment me. **/
    protected void setAdditionalParameters(final ISearchParameters params) {
        super.setAdditionalParameters(params);
        params.setParameter(new SearchParameter(BaseParameter.TYPE, SearchType.SEARCH_ADVANCED.getValueString()));
    }

    // Implementation of advanced query language. The spelling suggestions for
    // yellow and white only works as it should when the advanced query language
    // is used.
    /** TODO comment me. **/
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

    /** TODO comment me. **/
    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation(" (");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" OR ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(") ");
    }

    /** TODO comment me. **/
    protected void visitImpl(final DefaultOperatorClause clause) {
        boolean hasEmptyLeaf = false;

        hasEmptyLeaf |= isEmptyLeaf(clause.getFirstClause());
        hasEmptyLeaf |= isEmptyLeaf(clause.getSecondClause());

        clause.getFirstClause().accept(this);

        if (! hasEmptyLeaf){
            appendToQueryRepresentation(" AND ");
        }

        clause.getSecondClause().accept(this);
    }
    /** TODO comment me. **/
    protected void visitImpl(final NotClause clause) {
        appendToQueryRepresentation(" ANDNOT ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");

    }
    /** TODO comment me. **/
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

    private String correctQuery(
            final Map<String, List<SpellingSuggestion>> suggestions,
            String q) {

        // Query suggestions is returned in lowercase from Fast, including the keys that
        // maybe had mixed case. Lowers the case first to make the replacement work.
        q = q.toLowerCase();

        for (final List<SpellingSuggestion> suggestionList : suggestions.values()) {
            for (final SpellingSuggestion s : suggestionList) {
                q = q.replaceAll(s.getOriginal(), s.getSuggestion());
            }
        }

        return q;
    }
}
