/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;

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
 * A velocity directive to url encode a given request parameter. It's better to use
 * <code>#reqParamURLEncoded($request 'foo')</code> instead of
 * <code>#urlencode($request.getParameter('foo'))</code>, because then it's easier
 * to search for parameter-use that is not encoded in Velocity-templates.
 *
 * @author <a href="mailto:endre@sesam.no">Endre Midtgård Meckelborg</a>
 * @version <tt>$Revision: $</tt>
 */
public final class UrlEncodeRequestParameterDirective extends Directive {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(UrlEncodeRequestParameterDirective.class);

    /** Name of the directive. */
    private static final String NAME = "reqParamURLEncoded";

    /** Default charset for encoding. */
    private static final String DEFAULT_CHARSET = "utf-8";

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

            // If needed, we can implement logic if some params should not be encoded.
            final String value = request.getParameter(param);
            writer.write(URLEncoder.encode(value, DEFAULT_CHARSET));

            if (node.getLastToken().image.endsWith("\n")) {
                writer.write("\n");
            }

        }
        return true;
    }

}
