/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 * You can use, redistribute, and/or modify it, under the terms of the Possom License.
 * You should have received a copy of the Possom License along with this program.
 * If not, see https://dev.sesat.no/confluence/display/Possom/Possom+License
 */
package no.sesat.search.result.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.MimetypesFileTypeMap;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;


/**
 * Applies a regular expression to a specified field in every result item
 *  adding a target field matching the first capturing group in the regular expression.
 *
 *
 * @version $Id$
 */
public final class RegexpResultHandler implements ResultHandler {

    private final RegexpResultHandlerConfig config;

    private Pattern regExPattern;

    /**
     *
     * @param config
     */
    public RegexpResultHandler(final ResultHandlerConfig config){
        this.config = (RegexpResultHandlerConfig) config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {


        for (final ResultItem item : cxt.getSearchResult().getResults()) {

            final String field = item.getField(config.getField());

            if(null != field){
                if(null == regExPattern){
                    regExPattern = Pattern.compile(config.getRegexp());
                }

                final Matcher m = regExPattern.matcher(field);
                if(m.find()){

                    cxt.getSearchResult().replaceResult(item, item.addField(config.getTarget(), m.group(1)));
                }
            }
        }
    }
}

