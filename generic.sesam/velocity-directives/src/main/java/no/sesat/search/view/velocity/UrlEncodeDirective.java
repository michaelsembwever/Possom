/* Copyright (2006-2007) Schibsted Søk AS
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
 * UrlEncodeDirective.java
 *
 * Created on February 6, 2006, 3:45 PM
 *
 */

package no.sesat.search.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import org.apache.log4j.Logger;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *
 * A velocity directive to do url encoding.
 *  <b>Encodes ampersands</b> so the directive cannot be used to encode multiple parameters in one go.
 *
 * <code>
 * #urlencode('q=hej')
 * #urlencode('q=hej', 'iso-8859-1')
 * </code>
 *
 * The default charset is utf-8.
 *
 *
 * @version $Id$
 */
public final class UrlEncodeDirective extends Directive {

    private static final Logger LOG = Logger.getLogger(UrlEncodeDirective.class);

    private static final String NAME = "urlencode";
    private static final String DEFAULT_CHARSET = "utf-8";

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public int getType() {
        return LINE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean render(
            final InternalContextAdapter context,
            final Writer writer,
            final Node node)
                throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {



        if (node.jjtGetNumChildren() < 1) {
            rsvc.error("#" + getName() + " - missing argument");

        }else{

            String charset = DEFAULT_CHARSET;

            final String input = null != node.jjtGetChild(0).value(context)
                    ? node.jjtGetChild(0).value(context).toString()
                    : "";

            if(0 < input.length()){
                if (node.jjtGetNumChildren() == 2) {
                    charset = node.jjtGetChild(1).value(context).toString();
                }

                writer.write(URLEncoder.encode(input, charset)
                        .replace("'", "%27")
                        .replace("(", "%28")
                        .replace(")", "%29")
                        .replace("&", "%26"));

                final Token lastToken = node.getLastToken();

                if (lastToken.image.endsWith("\n")) {
                    writer.write("\n");
                }
            }
            return true;
        }
        return false;
    }

}
