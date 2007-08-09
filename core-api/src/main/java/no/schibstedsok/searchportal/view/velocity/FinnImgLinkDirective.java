/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
/*
 * FinnImgLinkDirective.java
 *
 * Created on 5. september 2006, 11:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import org.apache.log4j.Logger;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *
 * A velocity directive to return url for images on Finn torget.
 *
 * @author SSTHKJER
 */
public final class FinnImgLinkDirective extends Directive {
    
    private static final Logger LOG = Logger.getLogger(FinnImgLinkDirective.class);

    private static final String NAME = "finnImgLink";

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
        if (node.jjtGetNumChildren() < 1 && node.jjtGetNumChildren() > 2) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        String varName = null;
        if (node.jjtGetNumChildren() == 2) {
            varName = node.jjtGetChild(1).value(context).toString();
        }
        
        final String prefix = "http://cache.finn.no/mmo/";
        final String thumb = "_thumb";

        final String input = node.jjtGetChild(0).value(context).toString();
        String url = "";

        if (input.startsWith("prod/"))
            url = input.substring(5);

        url = prefix + input.substring(0, input.lastIndexOf(".")) + thumb + input.substring(input.lastIndexOf("."));;

        if (varName == null) {
            writer.write(url);
        } else {
            context.put(varName, url);
        }
        
        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }    
}
