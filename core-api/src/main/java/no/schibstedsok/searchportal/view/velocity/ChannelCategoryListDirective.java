// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.DocumentContext;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.util.Channel;
import no.schibstedsok.searchportal.util.Channels;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

public class ChannelCategoryListDirective extends Directive {

    private static final String NAME = "cclist";
    
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
        if (node.jjtGetNumChildren() !=  1) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        final String[] myChannels = node.jjtGetChild(0).value(context).toString().split(",");
        final Site site = (Site) context.get("site");
        final Channels.Context siteContext = ContextWrapper.wrap(
            
            Channels.Context.class,
            new SiteContext() {
                public Site getSite() {
                    return site;
                }
            },
            new DocumentContext() {
                public DocumentLoader newDocumentLoader(SiteContext siteCxt, String resource, DocumentBuilder builder) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }
        );
        
        ArrayList<Channel.Category> categories = new ArrayList<Channel.Category>();
        Channels channels = Channels.valueOf(siteContext);
        for (String id : myChannels) {
            final Channel channel = channels.getChannel(id);
            if (channel != null && !categories.contains(channel.getCategory())) {
                categories.add(channel.getCategory());
            }
        }
        
        Collections.sort(categories);
        
        context.put("numberOfMyChannels", myChannels.length);
        context.put("cclist", categories);
        return true;
    }
}
