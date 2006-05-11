/*
 * HtmlEscapeDirective.java
 *
 * Created on February 6, 2006, 5:34 PM
 *
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
 * A velocity directive to escape HTML.
 *
 * <code>
 * #htmlescape("<h1>html</h1>')
 * </code>
 *
 * @author magnuse
 */
public final class HtmlEscapeDirective extends Directive {

    private static final String NAME = "htmlescape";

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
            rsvc.error("#" + getName() + " - wrong number of argumants");
            return false;
        }

        final String input = node.jjtGetChild(0).value(context).toString();

        writer.write(StringEscapeUtils.escapeHtml(input));

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
}
