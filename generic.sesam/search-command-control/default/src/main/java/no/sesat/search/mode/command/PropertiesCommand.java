/* Copyright (2008) Schibsted Søk AS
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
 *
 * PropertiesCommand.java
 *
 *
 */

package no.sesat.search.mode.command;

import java.util.Map;
import no.sesat.search.mode.config.PropertiesCommandConfig;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

/** Simple command that searches in the specified properties map's keys.
 * If any key appears within the query (as complete words) the key and value are placed into the result item.
 * The command is useful for quick and dirty enrichments that match a small list of words.
 *
 * The following is a tutorial for a simple enrichment using this command
 * https://dev.schibstedsok.no/confluence/display/SESAT/Developing+a+quick+and+simple+Enrichment+tutorial
 *
 * @version $Id$
 *
 */
public final class PropertiesCommand extends AbstractSearchCommand {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(PropertiesCommand.class);

    private static final String KEY = "key";
    private static final String VALUE = "value";


    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of PropertiesCommand
     * @param cxt
     */
    public PropertiesCommand(final Context cxt) {

        super(cxt);
    }

    // Public --------------------------------------------------------

    public ResultList<ResultItem> execute() {

        final ResultList<ResultItem> result = new BasicResultList<ResultItem>();
        result.setHitCount(0);

        final PropertiesCommandConfig config = (PropertiesCommandConfig)getSearchConfiguration();
        final String query = getTransformedQuery();

        for(Map.Entry<String,String> entry : config.getProperties().entrySet()){
            if(query.matches(".*(^| )" + entry.getKey().toLowerCase() + "($| ).*")){
                ResultItem item = new BasicResultItem();
                item = item.addField(KEY, entry.getKey());
                item = item.addField(VALUE, entry.getValue());
                result.addResult(item);
                result.setHitCount(result.getHitCount() +1);
            }
        }
        return result;
    }

    // Package protected ---------------------------------------------


    @Override
    public String getQueryRepresentation() {
        return datamodel.getQuery().getString().toLowerCase();
    }

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------


    // Inner classes -------------------------------------------------

}
