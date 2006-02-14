/*
 * UrlEncodeDirective.java
 *
 * Created on February 6, 2006, 3:45 PM
 *
 */

package no.schibstedsok.front.searchportal.velocity;

import java.io.IOException;
import java.io.Writer;
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
public class UrlEncodeDirective extends Directive {
    
    private static transient Log log = LogFactory.getLog(UrlEncodeDirective.class);
    
    
    private static final String NAME = "urlencode";
    private static final String DEFAULT_CHARSET = "utf-8";
    
    /**
     * returns the name of the directive.
     *
     * @return the name of the directive.
     */
    public String getName() {
        return NAME;
    }
    
    /**
     * returns the type of the directive. The type is LINE.
     * @return The type == LINE
     */
    public int getType() {
        return LINE;
    }
    
    /**
     * Renders the urlencoded string.
     *
     * @param context
     * @param writer
     * @param node
     *
     * @throws java.io.IOException
     * @throws org.apache.velocity.exception.ResourceNotFoundException
     * @throws org.apache.velocity.exception.ParseErrorException
     * @throws org.apache.velocity.exception.MethodInvocationException
     * @return the encoded string.
     */
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() < 1) {
            rsvc.error("#" + getName() + " - missing argument");
            return false;
        }
        
        String charset = DEFAULT_CHARSET;
        
        String s = node.jjtGetChild(0).value(context).toString();
        
        if (node.jjtGetNumChildren() == 2) {
            charset = node.jjtGetChild(1).value(context).toString();
        }
        
        writer.write(URLEncoder.encode(s, charset));
        
        Token lastToken = node.getLastToken();
        
        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }
        
        return true;
    }
}
