/* Copyright (2006-2008) Schibsted ASA
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
 */
package no.sesat.search.mode.command;


import no.sesat.search.mode.config.CorrectingFast4CommandConfig;
import no.sesat.search.query.Query;
import org.apache.log4j.Logger;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.result.WeightedSuggestion;

/**
 * This class can be extended to get the following behaviour.
 *
 * If the execution of the search command does not return any hits and if there
 * is an query available, correct the query and rerun the command.
 *
 * The default implementation looks at the hitCount == 0 and what relevant queries are available.
 *
 * <b>Performance improvement</b> can be gained by overridding correctQuery(string) and calling
 * createQuery(string, false) so that another round evaluation is disabled.
 *
 * @XXX refactor out this functionality to a search command functor class.
 *
 * @version $Id$
 */
public abstract class CorrectingFast4SearchCommand extends Fast4SearchCommand {

    // Constants -----------------------------------------------------

    public static final String CORRECTION_COUNT = "correctionCount";

    private static final String ERR_CANNOT_CREATE_COMMAND = "Unable to create command to rerun.";

    private static final Logger LOG = Logger.getLogger(CorrectingFast4SearchCommand.class);

    // Attributes ----------------------------------------------------

    private int correctionCount = 0;
    private final Context cxt;

    // XXX couldn't we re-use functionality given by overriding AbstractSearchCommand.getQuery()
    private ReconstructedQuery correctedQuery;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of CorrectionFastSearchCommand.
     *
     * @param cxt Search command context.
     */
    public CorrectingFast4SearchCommand(final Context cxt) {
        super(cxt);
        this.cxt = cxt;
    }

    // Public --------------------------------------------------------

    @Override
    public final Query getQuery() {
        return correctedQuery != null ? correctedQuery.getQuery() : super.getQuery();
    }

    @Override
    public ResultList<ResultItem> call() {

        ResultList<ResultItem> result = super.call();

        // TODO Consider moving the isCorrectionEnabled() call after the
        // correction has been made and then discarding the result
        // should the call return false.
        // Sub classes might not know if the corrected query should be used
        // until after the query has been run. or at least not after a token
        // evaluation has been run on the corrected query.
        if (getCorrectionCount() < getSearchConfiguration().getCorrectingLimit()) {

            // Correct query and parse the resulting query string.
            final String oldQuery = datamodel.getQuery().getString();
            final String newQuery = correctQuery(result, oldQuery);

            if(!oldQuery.equalsIgnoreCase(newQuery)){

                try {

                    // Create a new command
                    final CorrectingFast4SearchCommand c = createCommand(cxt);

                    // and update it to use the corrected query
                    c.setCorrectedQuery(newQuery);
                    c.initialiseTransformedTerms(c.getQuery());

                    // now execute the command like normal
                    result = c.call();

                    // update this command's query to mirror the corrected command
                    datamodel.getSearch(getSearchConfiguration().getId()).setQuery(
                            datamodel.getSearch(c.getSearchConfiguration().getId()).getQuery());

                    // how many times correction has taken place is useful information
                    result = result.addObjectField(CORRECTION_COUNT, c.correctionCount);

                } catch (Exception ex) {
                    LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                }
            }
        }

        return result;
    }

    @Override
    public CorrectingFast4CommandConfig getSearchConfiguration() {
        return (CorrectingFast4CommandConfig)super.getSearchConfiguration();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected CorrectingFast4SearchCommand createCommand(final SearchCommand.Context cmdCxt) throws Exception {

        final Class<? extends CorrectingFast4SearchCommand> clazz = getClass();
        final Constructor<? extends CorrectingFast4SearchCommand> con = clazz.getConstructor(Context.class);

        final CorrectingFast4SearchCommand command = con.newInstance(cmdCxt);

        // note the number of times correction has occurred so far
        command.correctionCount = getCorrectionCount() + 1;
        return command;
    }

    protected String correctQuery(
            final ResultList<ResultItem> results,
            String q) {

        if(0 == results.getHitCount()){

            final Map<String, List<WeightedSuggestion>> suggestions
                    = ((BasicResultList<?>)results).getSpellingSuggestionsMap();

            // Query suggestions is returned in lowercase from Fast, including the keys that
            // maybe had mixed case. Lowers the case first to make the replacement work.
            q = q.toLowerCase();

            for (final List<WeightedSuggestion> suggestionList : suggestions.values()) {
                for (final WeightedSuggestion s : suggestionList) {
                    q = q.replaceAll(s.getOriginal(), s.getSuggestion());
                }
            }
        }

        return q;
    }

    protected final int getCorrectionCount(){
        return correctionCount;
    }

    protected void setCorrectedQuery(final String correctedQuery) {

        setCorrectedQuery(createQuery(correctedQuery));
    }

    protected final void setCorrectedQuery(final ReconstructedQuery query){

        this.correctedQuery = query;
    }

    // private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
