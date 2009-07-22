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


import no.sesat.search.mode.config.CorrectingSolrCommandConfig;
import no.sesat.search.query.Query;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

/** Supplements CorrectingSearchCommand behaviour to the SolrSearchCommand.
 *
 * correctQuery(..) delegates to CorrectingSearchCommandUtility.correctQueryFromSpellingSuggestions(results, q)
 *  and the spelling suggestions returned from the solr index.
 *
 * @version $Id$
 */
public class CorrectingSolrSearchCommand extends SolrSearchCommand implements CorrectingSearchCommand {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(CorrectingSolrSearchCommand.class);

    // Attributes ----------------------------------------------------

    private int correctionCount = 0;

    // XXX couldn't we re-use functionality given by overriding AbstractSearchCommand.getQuery()
    private ReconstructedQuery correctedQuery;

    private volatile boolean inCall = false;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of CorrectionFastSearchCommand.
     *
     * @param cxt Search command context.
     */
    public CorrectingSolrSearchCommand(final Context cxt) {
        super(cxt);
    }

    // Public --------------------------------------------------------

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
    public final Query getQuery() {
        return correctedQuery != null ? correctedQuery.getQuery() : super.getQuery();
    }

    @Override
    public CorrectingSolrCommandConfig getSearchConfiguration() {
        return (CorrectingSolrCommandConfig)super.getSearchConfiguration();
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
    public String correctQuery(final ResultList<ResultItem> results, String q) {

        return CorrectingSearchCommandUtility.correctQueryFromSpellingSuggestions(results, q);
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
