/*
 * Copyright (2005-22007 Schibsted Søk AS
 *   This file is part of SESAT.
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
package no.sesat.search.view.velocity;

import java.io.IOException;

import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * A velocity directive to remove a prefix if it exists.
 *
 * If 'html' is used for encoding the string will be HTML escaped, otherwise it defaults to url encoded.
 *
 * <code>
 * #removePrefix('this is the string to be checked', 'prefix to be removed', 'encoding to use')
 * </code>
 *
 * @author ajamtli
 */
public final class RemovePrefixDirective extends Directive {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(RemovePrefixDirective.class);

    /** Name of directive. */
    private static final String NAME = "removePrefix";

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
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() < 3) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        /* Encoding from Sesam is always UTF-8 */
        final String s = URLDecoder.decode(node.jjtGetChild(0).value(context).toString(),"UTF-8");
        final String prefix = node.jjtGetChild(1).value(context).toString();

        /* Encoding to use depends on target site. */
        final String encoding = node.jjtGetChild(2).value(context).toString();
        String returnString = s;

        if (s.toLowerCase().startsWith(prefix)) {
            returnString = s.substring(prefix.length());
            returnString = returnString.trim();
        }

        if ("html".equalsIgnoreCase(encoding) || "xml".equalsIgnoreCase(encoding)) {
            // sesat prefers xhtml
            writer.write(StringEscapeUtils.escapeXml(returnString));
        } else {
            writer.write(URLEncoder.encode(returnString, encoding));
        }

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
}
