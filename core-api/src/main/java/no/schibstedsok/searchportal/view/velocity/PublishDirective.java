// Copyright (2006-2007) Schibsted Søk AS
/*
 * PublishDirective.java
 *
 *
 */

package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketTimeoutException;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.view.ImportPublish;
import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

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
            ImportPublish.importPage(url, getDataModel(context), writer);
            return true;

        } catch (SocketTimeoutException ste) {
            LOG.error(ERR_NETWORK_DOWN + url + " --> " + ste.getMessage());

        }catch(IOException se){
            LOG.error(ERR_NETWORK_DOWN + url, se);
        }

        return false;
    }
}
