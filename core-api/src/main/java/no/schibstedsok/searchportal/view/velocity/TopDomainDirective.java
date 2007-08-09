/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
/*
 * TopDomainDirective.java
 *
 * Created on 26. september 2006, 10:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import org.apache.log4j.Logger;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * A velocity directive to take out the topdomain for picsearch
 *
 * @author SSTHKJER
 */
public final class TopDomainDirective extends Directive {
    
    private static final Logger LOG = Logger.getLogger(TopDomainDirective.class);

    private static final String NAME = "topDomain";

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
        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - missing argument");
            return false;
        }

        final String input = node.jjtGetChild(0).value(context).toString();
        String string = "";

        if (input.indexOf("http://") > -1) 
            string = input.substring(7);
        else if (input.indexOf("https://") > -1) { 
            string = input.substring(8);
        } else {
            string = input;
        }

        final int i = string.indexOf("/");

        final String topDomain = i > 0 ? string.substring(0, i) : string;

        writer.write(topDomain);

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
    
}
