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
import no.sesat.search.view.FindResource;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *
 *
 *
 * @version $Id$
 */
public final class ImageDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(ImageDirective.class);

    private static final String NAME = "image";

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

        if (argCount > 0 && argCount < 3) {

            final String image = "/images/" + getArgument(context, node, 0);
            final String attributes = getArgument(context, node, 1);

            final String url = FindResource.find(getDataModel(context).getSite().getSite(), image);

            writer.write("<img src=\"" + url + "\" " + attributes + "/>");

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
