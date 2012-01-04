/* Copyright (2012) Schibsted ASA
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

import java.io.IOException;

import java.io.Writer;
import no.sesat.search.result.StringChopper;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *
 * A velocity directive to chop a string
 * If a third parameter "esc" is sent, the string will also be htmlescaped
 *
 * <code>
 * #chopString('this string is being chopped!' 20)
 * returns the string: "this string is.."
 * </code>
 *
 *
 *
 * @version $Id$
 */
public final class ChopStringDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(ChopStringDirective.class);

    private static final String NAME = "chopString";

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

        final int argCount = node.jjtGetNumChildren();

        if (argCount > 0 && argCount < 5) {

            final Object nodeValue = node.jjtGetChild(0).value(context);

            if(nodeValue == null) {
                // No need to do anything since the string is empty anyway
                writer.write("");
                return true;

            }
            final String s = nodeValue.toString();

            final int length = argCount > 1
                    ? Integer.parseInt(getArgument(context, node, 1))
                    : Integer.MAX_VALUE;

            final boolean esc = argCount > 2 && "esc".equals(getArgument(context, node, 2));

            final boolean chopWord = argCount > 3 && Boolean.parseBoolean(getArgument(context, node, 3));

            final String chopSuey = esc
                    ? StringEscapeUtils.escapeXml(StringChopper.chop(s, length, chopWord))
                    : StringChopper.chop(s, length, chopWord);

            writer.write(chopSuey);

            if (node.getLastToken().image.endsWith("\n")) {
                writer.write('\n');
            }

        }else{

            final String msg = '#' + getName() + " - wrong number of arguments";
            LOG.error(msg);
            rsvc.getLog().error(msg);
            return false;
        }

        return true;

    }
}
