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
package no.sesat.search.view.velocity;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Writer;
import java.io.IOException;
import java.net.URLDecoder;

/**
 *
 * A velocity directive to do url decoding.
 *
 * <code>
 * #urlencode('&q=hej')
 * #urlencode('&q=hej', 'iso-8859-1')
 * </code>
 *
 * The default encoding is utf-8.
 *
 *
 */
public final class UrlDecodeDirective extends Directive {

    private static final Logger LOG = Logger.getLogger(UrlEncodeDirective.class);

    private static final String NAME = "urldecode";
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
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() < 1) {
            rsvc.error("#" + getName() + " - missing argument");
            return false;
        }

        String charset = DEFAULT_CHARSET;

        final String input = node.jjtGetChild(0).value(context).toString();

        if (node.jjtGetNumChildren() == 2) {
            charset = node.jjtGetChild(1).value(context).toString();
        }

        writer.write(URLDecoder.decode(input, charset));

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
}
