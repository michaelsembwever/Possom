/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
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

/** Supplements CorrectingSearchCommand behaviour to the Fast4SearchCommand.
 *
 * correctQuery(..) delegates to CorrectingSearchCommandUtility.correctQueryFromSpellingSuggestions(results, q)
 *  and the spelling suggestions returned from the fast index.
 *
 * @version $Id$
 */
public class CorrectingFast4SearchCommand extends Fast4SearchCommand implements CorrectingSearchCommand {

    // Constants -----------------------------------------------------

    public static final String CORRECTION_COUNT = "correctionCount";

    private static final String ERR_CANNOT_CREATE_COMMAND = "Unable to create command to rerun.";

    private static final Logger LOG = Logger.getLogger(CorrectingFast4SearchCommand.class);

    // Attributes ----------------------------------------------------
    private volatile boolean inCall = false;

    private int correctionCount = 0;

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
    }

    // Public --------------------------------------------------------

    @Override
    public final Query getQuery() {
        return correctedQuery != null ? correctedQuery.getQuery() : super.getQuery();
    }

    @Override
    public ResultList<ResultItem> call() {

        final boolean wasInCall = inCall;
        try{
            inCall = true;

            return wasInCall
                    ? super.call()
                    : CorrectingSearchCommandUtility.doCall(this, context);
        }finally{
            inCall = false;
        }
    }

    @Override
    public CorrectingFast4CommandConfig getSearchConfiguration() {
        return (CorrectingFast4CommandConfig)super.getSearchConfiguration();
    }

    @Override
    public String correctQuery(final ResultList<ResultItem> results, String q) {

        return CorrectingSearchCommandUtility.correctQueryFromSpellingSuggestions(results, q);
    }

    @Override
    public final int getCorrectionCount(){
        return correctionCount;
    }

    @Override
    public final void setCorrectionCount(final int count){
        this.correctionCount = count;
    }

    @Override
    public void setCorrectedQuery(final String correctedQuery) {

        setCorrectedQuery(createQuery(correctedQuery));
        initialiseTransformedTerms(getQuery());
    }

    @Override
    public CorrectingSearchCommand initialiseNewCommand(final  CorrectingSearchCommand command){
        return command;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected final void setCorrectedQuery(final ReconstructedQuery query){

        this.correctedQuery = query;
    }

    // private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
