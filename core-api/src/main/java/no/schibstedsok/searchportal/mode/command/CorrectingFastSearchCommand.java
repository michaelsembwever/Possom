// Copyright (2005-2007) Schibsted Søk AS
/*
 * CorrectingFastSearchCommand.java
 *
 * Created on August 29, 2006, 1:08 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;
import org.apache.log4j.Logger;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 *
 * This class can be extended to get the following behaviour.
 *
 * If the execution of the search command does not return any hits and if there
 * are spelling suggestions available, correct the query and rerun the command.
 *
 * @author maek
 */
public abstract class CorrectingFastSearchCommand extends AdvancedFastSearchCommand {

    private static final String ERR_CANNOT_CREATE_COMMAND =
            "Unable to create command to rerun.";
    private static final String RESULT_FIELD_CORRECTED_QUERY =
            "autoCorrectedQuery";

    private static final Logger LOG = Logger.getLogger(CorrectingFastSearchCommand.class);

    private boolean correct = true;

    /** Creates a new instance of CorrectionFastSearchCommand.
     *
     * @param cxt Search command context.
     */
    public CorrectingFastSearchCommand(final Context cxt) {

        super(cxt);
    }

    /** {@inheritDoc} */
    public SearchResult execute() {

        final SearchResult originalResult = super.execute();
        final Map<String, List<SpellingSuggestion>> suggestions = originalResult.getSpellingSuggestions();

        // Rerun command?
        // TODO Consider moving the isCorrectionEnabled() call after the
        // correction has been made and then discarding the result
        // should the call return false.
        // Sub classes might not know if the corrected query should be used
        // until after the query has been run. or at least not after a token
        // evaluation has been run on the corrected query.
        if (isCorrectionEnabled() && correct && originalResult.getHitCount() == 0 && !suggestions.isEmpty()) {
            // Correct spelling suggestions and parse the resulting query string.
            final String oldQuery = datamodel.getQuery().getString();
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

    private CorrectingFastSearchCommand createCommand(final SearchCommand.Context cmdCxt) throws Exception {
        final Class<? extends CorrectingFastSearchCommand> clazz = getClass();
        final Constructor<? extends CorrectingFastSearchCommand> con
                = clazz.getConstructor(Context.class);
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
