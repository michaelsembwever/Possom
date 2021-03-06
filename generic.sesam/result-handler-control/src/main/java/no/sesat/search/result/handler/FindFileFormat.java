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

import javax.activation.MimetypesFileTypeMap;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;


/**
 * @version itthkjer
 * @version $Id$
 */
public final class FindFileFormat implements ResultHandler {

    private final FindFileFormatResultHandlerConfig config;

    private static final MimetypesFileTypeMap TYPES = new MimetypesFileTypeMap();

    /**
     *
     * @param config
     */
    public FindFileFormat(final ResultHandlerConfig config){
        this.config = (FindFileFormatResultHandlerConfig) config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {


        for (final ResultItem item : cxt.getSearchResult().getResults()) {

            final String type = null != config.getField() ? item.getField(config.getField()) : null;
            final String url = item.getField(config.getUrlField());

            final String fileformat = null != type && type.length() > 0
                    ? type
                    : ("application/octet-stream".equals(TYPES.getContentType(url))
                        ? "text/html"
                        : TYPES.getContentType(url));


            cxt.getSearchResult().replaceResult(item, item.addField("fileformat", fileformat));
        }
    }
}
