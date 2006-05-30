package no.schibstedsok.front.searchportal.view.velocity;

import java.io.IOException;

import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
 * <code>
 * #removePrefix('this is the string to be checked', 'prefix to be removed', 'encoding to use')
 * </code>
 *
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

        /* TODO: Remove this when NRK fix their special character bug or we are no longer linking to NRK search */
        boolean nrkFix = false;
        if (node.jjtGetNumChildren() == 4) {
            nrkFix = node.jjtGetChild(3).value(context).toString().equals("true");
        }
        
        /* Encoding from Sesam is always UTF-8 */
        final String s = URLDecoder.decode(node.jjtGetChild(0).value(context).toString(),"UTF-8");
        final String prefix = node.jjtGetChild(1).value(context).toString();
        
        /* Encoding to use depends on target site. */
        final String encoding = node.jjtGetChild(2).value(context).toString();
       
        String returnString = s;
        if (s.startsWith(prefix)) {
            returnString = s.substring(prefix.length());
            returnString = returnString.trim();
        }
        
        /* TODO: to be removed when above todo is done */
        if (nrkFix) {
            returnString = returnString.replaceAll("[&*?<>]", " ");
        }
        
        writer.write(URLEncoder.encode(returnString, encoding));
        
        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
}
