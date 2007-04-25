// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import javax.activation.MimetypesFileTypeMap;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;


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


        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {

            final String type = null != config.getField() ? item.getField(config.getField()) : null;
            final String url = item.getField("url");

            final String fileformat = null != type && type.length() > 0
                    ? type
                    : ("application/octet-stream".equals(TYPES.getContentType(url))
                        ? "text/html"
                        : TYPES.getContentType(url));


            item.addField("fileformat", fileformat);
        }
    }
}
