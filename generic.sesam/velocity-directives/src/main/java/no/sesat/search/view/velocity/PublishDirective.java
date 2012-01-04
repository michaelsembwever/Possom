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
/*
 * PublishDirective.java
 *
 *
 */

package no.sesat.search.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketTimeoutException;
import no.sesat.search.view.ImportPublish;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.site.Site;
import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.app.VelocityEngine;

/**
 *
 * A velocity directive to import page fragments from publishing system.
 *
 * <code>
 * #publish('pages/front.html')
 * </code>
 *
 *
 *
 * @version $Id$
 */
public final class PublishDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(PublishDirective.class);
    private static final String ERR_NETWORK_DOWN = "Network down? ";

    private static final String NAME = "publish";

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

        if (node.jjtGetNumChildren() < 1) {
            rsvc.error("#" + getName() + " - missing argument");
            return false;
        }

        // The argument gets url encoded on the way in. Make sure to decode the / characters.
        final String url = getArgument(context, node, 0).replaceAll("%2F", "/");

        try{

            if(1 == node.jjtGetNumChildren() && !url.endsWith(".xml")) {
                writer.write(ImportPublish.importPage(url, getDataModel(context)));
            }else{
                final DataModel dataModel = getDataModel(context);
                final Site site = dataModel.getSite().getSite();
                final VelocityEngine engine = VelocityEngineFactory.valueOf(dataModel.getSite().getSite()).getEngine();

                context.put("document", ImportPublish.importXml(url, dataModel));

                if (2 == node.jjtGetNumChildren()) {
                    VelocityEngineFactory.getTemplate(engine, site, getArgument(context, node, 1)).merge(context, writer);
                }
            }
            return true;

        } catch (SocketTimeoutException ste) {
            LOG.error(ERR_NETWORK_DOWN + url + " --> " + ste.getMessage());
        } catch(Exception e) {
            LOG.error(ERR_NETWORK_DOWN + url , e);
        }

        return false;
    }
}
