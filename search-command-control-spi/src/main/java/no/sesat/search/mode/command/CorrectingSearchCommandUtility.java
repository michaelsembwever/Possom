/* Copyright (2009) Schibsted ASA
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import no.sesat.search.mode.command.SearchCommand.Context;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.result.WeightedSuggestion;
import org.apache.log4j.Logger;

/**
 * Utility class to help implement CorrectingSearchCommand.
 *
 * If the execution of the search command does not return any hits and if there
 * is an query available, correct the query and rerun the command.
 *
 * <b>Performance improvement</b> can be gained by overridding correctQuery(string) and calling
 * createQuery(string, false) so that another round evaluation is disabled.
 *
 * @version $Id$
 */
public final class CorrectingSearchCommandUtility {

    // Constants -----------------------------------------------------

    public static final String CORRECTION_COUNT = "correctionCount";

    private static final Logger LOG = Logger.getLogger(CorrectingSearchCommandUtility.class);
    private static final String ERR_CANNOT_CREATE_COMMAND = "Unable to create command to rerun.";

    // Static --------------------------------------------------------

    /** This is called from the CorrectingSearchCommand.call() method.
     * Since the first thing done here is to callback the same .call() method
     * trouble must be taken to avoid the never ending loop by detecting and averting the recursive with code like:
        <code>
          public SomeSearchCommand extends .. implements CorrectingSearchCommand{
            private volatile boolean inCall = false;
            ...
            @Override
            public ResultList call() {

                try{
                    inCall = true;
                    return inCall
                            ? super.call()
                            : CorrectingSearchCommandUtility.doCall(this, context);
                }finally{
                    inCall = false;
                }
            }
            ...
          }
        </code>
     *
     * @param command
     * @param cxt
     * @return
     */
    public static ResultList<ResultItem> doCall(final CorrectingSearchCommand command, final Context cxt) {

        ResultList<ResultItem> result = command.call();

        // TODO Consider moving the isCorrectionEnabled() call after the
        // correction has been made and then discarding the result
        // should the call return false.
        // Sub classes might not know if the corrected query should be used
        // until after the query has been run. or at least not after a token
        // evaluation has been run on the corrected query.
        if (command.getCorrectionCount() < command.getSearchConfiguration().getCorrectingLimit()) {

            // Correct query and parse the resulting query string.
            final String commandId = cxt.getSearchConfiguration().getId();
            final String oldQuery = cxt.getDataModel().getSearch(commandId).getQuery().getString();
            final String newQuery = command.correctQuery(result, oldQuery);

            if(!oldQuery.equalsIgnoreCase(newQuery)){

                try {

                    // Create a new command
                    final CorrectingSearchCommand c = command.initialiseNewCommand(createCommand(cxt, command));

                    // and update it to use the corrected query
                    c.setCorrectedQuery(newQuery);

                    // now execute the command like normal
                    result = c.call();

//                    // update this command's query to mirror the corrected command
//                    cxt.getDataModel().getSearch(cxt.getSearchConfiguration().getId())
//                            .setQuery(cxt.getDataModel().getSearch(c.getSearchConfiguration().getId()).getQuery());

                    // how many times correction has taken place is useful information
                    result = result.addObjectField(CORRECTION_COUNT, c.getCorrectionCount());

                } catch (NoSuchMethodException ex) {
                    LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                } catch (InstantiationException ex) {
                    LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                } catch (IllegalAccessException ex) {
                    LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                } catch (IllegalArgumentException ex) {
                    LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                } catch (InvocationTargetException ex) {
                    LOG.error(ERR_CANNOT_CREATE_COMMAND, ex);
                }
            }
        }

        return result;
    }

    /**
     * Corrects the query by looking at the hitCount == 0 and what relevant queries are available.
     *
     * @param results
     * @param q
     * @return
     */
    public static String correctQueryFromSpellingSuggestions(
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

    // Constructors --------------------------------------------------

    private CorrectingSearchCommandUtility(){}

    // private -------------------------------------------------------

    private static CorrectingSearchCommand createCommand(
            final SearchCommand.Context cmdCxt,
            final CorrectingSearchCommand oldCommand)
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException  {

        final Class<? extends CorrectingSearchCommand> clazz = oldCommand.getClass();
        final Constructor<? extends CorrectingSearchCommand> con = clazz.getConstructor(Context.class);

        final CorrectingSearchCommand command = con.newInstance(cmdCxt);

        // note the number of times correction has occurred so far
        command.setCorrectionCount(oldCommand.getCorrectionCount() + 1);
        return command;
    }

}
