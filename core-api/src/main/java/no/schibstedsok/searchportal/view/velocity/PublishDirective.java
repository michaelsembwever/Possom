/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
/*
 * PublishDirective.java
 *
 *
 */

package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketTimeoutException;
import no.schibstedsok.searchportal.view.ImportPublish;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.site.Site;
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
 * @author mick
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
