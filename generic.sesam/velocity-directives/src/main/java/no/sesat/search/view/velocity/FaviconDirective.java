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
package no.sesat.search.view.velocity;

import java.io.IOException;

import java.io.Writer;
import no.sesat.search.view.FindResource;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *  This directive creates a link element used to specify the favicon element.
 *  The #favicon directive should be placed inside the head element of the page.
 *  The resolved resource name is used since some browsers (Opera) will not show the
 *  favicon when it is a redirect.
 *
 *  Example:
 *    #favicon("/images/favicon.gif")
 *    <link href="/genericno.localhost//images/1223283733372/favicon.gif" rel="icon" type="image/gif" />
 *
 *    #favicon("/images/favicon.gif" 'type="image/gif" rel="shortcut icon"')
 *    <link href="/genericno.localhost//images/1223284107022/favicon.gif" type="image/gif" rel="shortcut icon"/>
 *
 */
public final class FaviconDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(FaviconDirective.class);

    private static final String NAME = "favicon";

   /**
     * {@inheritDoc}
     */
    public int getType() {
        return LINE;
    }

    public String getName() {
        return NAME;
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
        final String resolvedPath = FindResource.find(getDataModel(context).getSite().getSite(), getArgument(context, node, 0));

        if (argCount == 1) {
            writer.write("<link href=\"" + resolvedPath + "\" rel=\"icon\" type=\"image/gif\" />");
        }
        if (argCount == 2) {
            writer.write("<link href=\"" + resolvedPath + "\" " + getArgument(context, node, 1) + "/>");
        }
        else{
            final String msg = '#' + getName() + " - wrong number of arguments";
            LOG.error(msg);
            rsvc.getLog().error(msg);
            return false;
        }

        return true;

    }
}
