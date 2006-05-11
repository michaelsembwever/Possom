/*
 * XmlEscapeDirective.java
 *
 * Created on April 28, 2006, 2:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.view.velocity;

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
 * @author maek
 */
public class XmlEscapeDirective extends Directive {

    private static final String NAME = "xmlescape";

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
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        if (node.jjtGetChild(0).value(context) != null) {

            final String input = node.jjtGetChild(0).value(context).toString();

            if (input != null) {
                writer.write(StringEscapeUtils.escapeXml(input));
            }

            final Token lastToken = node.getLastToken();

            if (lastToken.image.endsWith("\n")) {
                writer.write("\n");
            }

        }
        return true;
    }
}
