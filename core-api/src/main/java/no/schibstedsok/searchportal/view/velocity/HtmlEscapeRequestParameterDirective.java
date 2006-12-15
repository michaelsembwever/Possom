/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * A velocity directive to HTML escape a given request parameter. It's better to use
 * <code>#reqParamHTMLEscaped($request 'foo')</code> instead of
 * <code>#htmlescape($request.getParameter('foo'))</code>, because then it's easier
 * to search for parameter-use that is not escaped in Velocity-templates.
 *
 * @author <a href="mailto:endre@sesam.no">Endre Midtgård Meckelborg</a>
 * @version <tt>$Revision: $</tt>
 */
public final class HtmlEscapeRequestParameterDirective extends Directive {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(HtmlEscapeRequestParameterDirective.class);

    /** Name of the directive. */
    private static final String NAME = "reqParamHTMLEscaped";

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
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() != 2) {
            rsvc.error("#" + getName() + " - wrong number of argumants");
            return false;
        }

        if (node.jjtGetChild(1).value(context) != null) {
            final HttpServletRequest request = (HttpServletRequest) node.jjtGetChild(0).value(context);
            final String param = node.jjtGetChild(1).value(context).toString();

            // If needed, we can implement logic if some params should not be escaped.
            final String value = request.getParameter(param);
            writer.write(StringEscapeUtils.escapeHtml(value));

            if (node.getLastToken().image.endsWith("\n")) {
                writer.write("\n");
            }

        }
        return true;
    }

}
