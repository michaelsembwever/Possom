// Copyright (2006) Schibsted Søk AS
/*
 * UrlEncodeDirective.java
 *
 * Created on February 6, 2006, 3:45 PM
 *
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
 *
 * A velocity directive to do url encoding.
 *
 * <code>
 * #urlencode('&q=hej')
 * #urlencode('&q=hej', 'iso-8859-1')
 * </code>
 *
 * The default charset is utf-8.
 *
 * @author magnuse
 */
public final class UrlEncodeDirective extends Directive {

    private static final Logger LOG = Logger.getLogger(UrlEncodeDirective.class);

    private static final String NAME = "urlencode";
    private static final String DEFAULT_CHARSET = "utf-8";

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
        if (node.jjtGetNumChildren() < 1) {
            rsvc.error("#" + getName() + " - missing argument");
            return false;
        }

        String charset = DEFAULT_CHARSET;

        final String input = node.jjtGetChild(0).value(context) != null ? node.jjtGetChild(0).value(context).toString() : "";

        if (node.jjtGetNumChildren() == 2) {
            charset = node.jjtGetChild(1).value(context).toString();
        }

        writer.write(URLEncoder.encode(input, charset));

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
}
