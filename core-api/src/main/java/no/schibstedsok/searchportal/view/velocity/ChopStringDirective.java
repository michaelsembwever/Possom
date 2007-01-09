package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;

import java.io.Writer;
import no.schibstedsok.searchportal.result.StringChopper;

import org.apache.commons.lang.StringEscapeUtils;
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
 * A velocity directive to chop a string
 * If a third parameter "esc" is sent, the string will also be htmlescaped
 *
 * <code>
 * #chopString('this string is being chopped!' 20)
 * returns the string: "this string is.."
 * </code>
 *
 *
 * @author thomas
 * @version $Id$
 */
public final class ChopStringDirective extends Directive {

    private static final Logger LOG = Logger.getLogger(ChopStringDirective.class);
    
    private static final String NAME = "chopString";

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
    public boolean render(
                final InternalContextAdapter context, 
                final Writer writer, 
                final Node node) 
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        
        final int argCount = node.jjtGetNumChildren();
        
        if (argCount > 0 && argCount < 4) {

            final String s = node.jjtGetChild(0).value(context).toString();
            
            final int length = argCount > 1
                    ? Integer.parseInt(node.jjtGetChild(1).value(context).toString())
                    : Integer.MAX_VALUE;

            if (argCount > 2 && "esc".equals(node.jjtGetChild(2).value(context).toString())) {
                
                writer.write(StringEscapeUtils.escapeHtml(StringChopper.chop(s, length)));
            }else{
                
                writer.write(StringChopper.chop(s, length));
            }

            if (node.getLastToken().image.endsWith("\n")) {
                writer.write('\n');
            }
        
        }else{
            
            final String msg = '#' + getName() + " - wrong number of arguments";
            LOG.error(msg);
            rsvc.error(msg);
            return false;
        }

        return true;

    }
}
