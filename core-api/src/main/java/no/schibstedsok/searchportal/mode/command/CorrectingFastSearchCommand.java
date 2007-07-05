// Copyright (2005-2007) Schibsted Søk AS
/*
 * CorrectingFastSearchCommand.java
 *
 * Created on August 29, 2006, 1:08 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.query.Query;
import org.apache.log4j.Logger;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.result.WeightedSuggestion;

/**
 *
 * This class can be extended to get the following behaviour.
 *
 * If the execution of the search command does not return any hits and if there
 * are spelling suggestions available, correct the query and rerun the command.
 *
 * @author maek
 * @version $Id$
 */
public abstract class CorrectingFastSearchCommand extends AdvancedFastSearchCommand {
    
    private static final String ERR_CANNOT_CREATE_COMMAND = "Unable to create command to rerun.";
    
    private static final Logger LOG = Logger.getLogger(CorrectingFastSearchCommand.class);

    private boolean correct = true;
    private final Context cxt;
    // XXX couldn't we re-use functionality given by overriding AbstractSearchCommand.getQuery()
    private ReconstructedQuery correctedQuery;

    /** Creates a new instance of CorrectionFastSearchCommand.
     *
     * @param cxt Search command context.
     */
    public CorrectingFastSearchCommand(final Context cxt) {
        super(cxt);
        this.cxt = cxt;
    }

    private void setCorrectedQuery(final String correctedQuery) {
        this.correctedQuery = createQuery(correctedQuery);
    }

    @Override
    protected final Query getQuery() {
        return correctedQuery != null ? correctedQuery.getQuery() : super.getQuery();
    }

    /** {@inheritDoc} */
    @Override
    public ResultList<? extends ResultItem> call() {
        final ResultList<? extends ResultItem> originalResult = super.call();
        final Map<String, List<WeightedSuggestion>> suggestions
                = ((BasicResultList<?>)originalResult).getSpellingSuggestionsMap();

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

            try {
                // Create and execute command on corrected query.
                // Making sure this new command does not try to do the whole
                // correction thing all over again.
                final CorrectingFastSearchCommand c = createCommand(cxt);
                c.performQueryTransformation();
                c.correct = false;
                c.setCorrectedQuery(newQuery);

                final ResultList<? extends ResultItem> result = c.call();

                return result;

            } catch (Exception ex) {
                LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                return originalResult;
            }
        }

        return originalResult;
    }

    @Override
    protected void updateTransformedQuerySesamSyntax() {
                
        // redo the transformedQuerySesamSyntax off our correctedQuery (if it exists)
        initialiseTransformedTerms(getQuery());
        super.updateTransformedQuerySesamSyntax();
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
        final Constructor<? extends CorrectingFastSearchCommand> con = clazz.getConstructor(Context.class);
        
        return con.newInstance(cmdCxt);
    }
    
    private String correctQuery(
            final Map<String, List<WeightedSuggestion>> suggestions,
            String q) {
        
        // Query suggestions is returned in lowercase from Fast, including the keys that
        // maybe had mixed case. Lowers the case first to make the replacement work.
        q = q.toLowerCase();
        
        for (final List<WeightedSuggestion> suggestionList : suggestions.values()) {
            for (final WeightedSuggestion s : suggestionList) {
                q = q.replaceAll(s.getOriginal(), s.getSuggestion());
            }
        }
        
        return q;
    }
}
