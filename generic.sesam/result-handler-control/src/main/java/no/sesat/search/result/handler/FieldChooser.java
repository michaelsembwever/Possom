/* Copyright (2006-2008) Schibsted Søk AS
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
package no.sesat.search.result.handler;

import no.sesat.search.datamodel.DataModel;
import org.apache.log4j.Logger;

import java.util.Collection;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;


/** Copies the first found value in the list of from fields into the target field.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class FieldChooser implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(FieldChooser.class);

    private final ClassLoader cl = FieldChooserResultHandlerConfig.class.getClassLoader();
    private final FieldChooserResultHandlerConfig config;

    /**
     *
     * @param config
     */
    public FieldChooser(final ResultHandlerConfig config) {
        final ClassLoader cl = config.getClass().getClassLoader();
        this.config = (FieldChooserResultHandlerConfig) config;
    }

    public void handleResult(final Context cxt, final DataModel datamodel) {

        final Collection<String> fields = config.getFields();
        final ResultList<ResultItem> searchResult = cxt.getSearchResult();
        chooseField(searchResult, fields);
    }

    private void chooseField(final ResultList<ResultItem> searchResult, final Collection<String> fields) {

        if (searchResult != null) {

            for (ResultItem i : searchResult.getResults()) {

                ResultItem item = i;

                for (String field : fields) {
                    if (item.getField(field) != null) {
                        item = item.addField(config.getTarget(), item.getField(field));
                        break;
                    }
                }

                if (config.getDefaultValue() != null && item.getField(config.getTarget()) == null) {
                    item = item.addField(config.getTarget(), config.getDefaultValue());
                }

                if (item instanceof ResultList<?>) {
                    chooseField((ResultList<ResultItem>)item, fields);
                }

                searchResult.replaceResult(i, item);
            }

        }
    }


}
