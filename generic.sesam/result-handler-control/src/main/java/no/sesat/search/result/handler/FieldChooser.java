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
package no.sesat.search.result.handler;

import no.sesat.search.datamodel.DataModel;
import org.apache.log4j.Logger;

import java.util.Collection;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;


/** Copies the first found value in the list of from fields into the target field.
 *
 * Will copy objects [using addField(.., getObjectField(..))] if the field's value is not a string.
 * For fields with a string value addObjectField(.., getField(..)) will still be used.
 *
 * @version <tt>$Id$</tt>
 */
public final class FieldChooser implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(FieldChooser.class);

    private final FieldChooserResultHandlerConfig config;

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

        if (null != searchResult) {

            for (ResultItem i : searchResult.getResults()) {

                ResultItem item = i;

                for (String field : fields) {
                    if (null != item.getObjectField(field)) {
                        if(item.getObjectField(field) instanceof String){
                            item = item.addField(config.getTarget(), item.getField(field));
                        }else{
                            item = item.addObjectField(config.getTarget(), item.getObjectField(field));
                        }
                        break;
                    }
                }

                if (null != config.getDefaultValue() && null == item.getField(config.getTarget())) {
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
