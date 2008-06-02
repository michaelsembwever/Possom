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
 */
package no.sesat.search.view.velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.log4j.Logger;

import java.io.Writer;
import java.io.IOException;
import java.text.MessageFormat;

import java.util.Map;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.result.HitCount;
import no.sesat.search.result.ResultList;

/** Very simple directive to loop through all parameters as defined in the datamodel and write them out in one line.
 * Resulting output assured to be UTF8 url encoded.
 *
 *
 * Finishes with a trailing & (ampersand)
 *
 * http://sesat.no/scarab/issues/id/SKER4760
 *
 * @author <a href="mailto:mick@semb.wever.org">Mick</a>
 * @version $Id$
 */
public final class AllParametersDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(AllParametersDirective.class);

    private static final String NAME = "allParameters";
    private static final String ERR_MISSING_ARG = "#{0} - missing or invalid argument at {1}:{2},{3}";

    public String getName() {
        return NAME;
    }

    public int getType() {
        return LINE;
    }

    public boolean render(final InternalContextAdapter cxt, final Writer writer, final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (node.jjtGetNumChildren() != 0) {

            LOG.error(MessageFormat.format(
                    ERR_MISSING_ARG, getName(), cxt.getCurrentTemplateName(), node.getLine(), node.getColumn()));
            return false;
        }

        for(Map.Entry<String,StringDataObject> entry : getDataModel(cxt).getParameters().getValues().entrySet()){

            final String key = entry.getKey();
            // only include url parameters. currently private detail to DataModelFilter.updateDataModelForRequest(..)
            if(getDataModel(cxt).getParameters().getValues().containsKey(key + "-isUrl")){
                final String value = entry.getValue().getUtf8UrlEncoded();
                writer.append(key + '=' + value + '&');
            }
        }

        return true;
    }
}
