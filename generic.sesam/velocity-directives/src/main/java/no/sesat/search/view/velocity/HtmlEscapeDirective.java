/* Copyright (2007) Schibsted Søk AS
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
/*
 * HtmlEscapeDirective.java
 *
 * Created on February 6, 2006, 5:34 PM
 *
 */
package no.sesat.search.view.velocity;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *
 * A velocity directive to escape HTML.
 *
 * <code>
 * #htmlescape("<h1>html</h1>')
 * </code>
 *
 * @author magnuse
 * @version $Id$
 * @deprecated use XmlEscapeDirective instead as we render xhtml pages.
 */
public final class HtmlEscapeDirective extends Directive {

    private static final String NAME = "htmlescape";

    /**
     * {@inheritDoc}
     */
    public int getType() {
        return LINE;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - wrong number of argumants");
            return false;
        }

        
        if (node.jjtGetChild(0).value(context) != null)   {



        final String input = node.jjtGetChild(0).value(context).toString();

        writer.write(StringEscapeUtils.escapeHtml(input));

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }


        }
        return true;
        }
}
