/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.view.velocity;

import java.io.IOException;

import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.lang.StringEscapeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * A velocity directive to remove a prefix if it exists.
 *
 * If 'html' is used for encoding the string will be HTML escaped.
 *
 * <code>
 * #removePrefix('this is the string to be checked', 'prefix to be removed', 'encoding to use')
 * </code>
 *
 * @author ajamtli
 */
public final class RemovePrefixDirective extends Directive {

    /** Logger. */
    private static transient Log log = LogFactory.getLog(RemovePrefixDirective.class);

    /** Name of directive. */
    private static final String NAME = "removePrefix";

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
        if (node.jjtGetNumChildren() < 3) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        /* Encoding from Sesam is always UTF-8 */
        final String s = URLDecoder.decode(node.jjtGetChild(0).value(context).toString(),"UTF-8");
        final String prefix = node.jjtGetChild(1).value(context).toString();

        /* Encoding to use depends on target site. */
        final String encoding = node.jjtGetChild(2).value(context).toString();
        String returnString = s;

        // Remove the prefix unless it is "tv-<word>". If the user writes ie "tv-program",
        // we want the "tv" kept in the headings, to prevent the header "-program".
        if (!s.toLowerCase().startsWith("tv-") && s.toLowerCase().startsWith(prefix)) {
            returnString = s.substring(prefix.length());
            returnString = returnString.trim();
        }

        if ("html".equals(encoding)) {
            writer.write(StringEscapeUtils.escapeHtml(returnString));
        } else {
            writer.write(URLEncoder.encode(returnString, encoding));
        }

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
}
