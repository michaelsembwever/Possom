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
 */
package no.sesat.search.result.handler;

import org.apache.log4j.Logger;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;


/**
 * @see FieldSplitterResultHandlerConfig
 *
 * @version $Id$
 */
public final class FieldSplitter implements ResultHandler {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FieldSplitter.class);

    // Attributes ----------------------------------------------------

    private final FieldSplitterResultHandlerConfig config;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public FieldSplitter(final ResultHandlerConfig config) {
        this.config = (FieldSplitterResultHandlerConfig) config;
    }

    // Public --------------------------------------------------------

    public void handleResult(final Context cxt, final DataModel datamodel) {
        for (ResultItem item : cxt.getSearchResult().getResults()) {
            final String fieldValue = item.getField(config.getFromField());
            final ResultItem org = item;

            if (fieldValue != null) {
                final String[] split = fieldValue.split(config.getSeparator());

                for (final String s : split) {
                    item = item.addToMultivaluedField(config.getToField(), s.trim());
                }

                cxt.getSearchResult().replaceResult(org, item);
            }
        }
    }

    // Getters / Setters ---------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
