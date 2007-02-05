package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
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
 * A velocity directive to bold (query) word(s) 
 * param 1: text to replace
 * param 2: query
 *
 * - "og" and "i" in the query should not be bolded. 
 * - Capitalized words should stay capitalized
 * - remove "," and "!" from query words
 *
 * <code>
 * #boldWord('leter du etter hotell i Paris' 'hotell i paris')
 * returns the string: "leter du etter <b>hotell</b> i <b>Paris</b>"
 * </code>
 *
 *
 * @author thomas
 * @version $Id$
 */
public final class BoldWordDirective extends Directive {

    private static final Logger LOG = Logger.getLogger(BoldWordDirective.class);
    
    private static final String NAME = "boldWord";

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

        if (argCount != 1) {
            
            String text = node.jjtGetChild(0).value(context).toString();
            final String query = node.jjtGetChild(1).value(context).toString();
            
            if(text == null) {
                writer.write("");
                return true;
            }
                        
            String patternstr = "[\\p{Punct}\\p{Space}]+";
            String replace = "";
            String replaceUp = "";
            List list = Arrays.asList(query.split(patternstr));
            for (int i=0;i<list.size();i++) {
                if (!list.get(i).toString().toLowerCase().equals("og") && !list.get(i).toString().toLowerCase().equals("i")) {
                    replace = "<b>" + list.get(i) + "</b>";
                    replaceUp = "<b>" + StringUtils.capitalize(list.get(i).toString()) + "</b>";
                    text = text.replaceAll(list.get(i).toString(), replace);
                    text = text.replaceAll(StringUtils.capitalize(list.get(i).toString()), replaceUp);
                    //text = text.replaceAll("(?i)" + list.get(i), rep);
                }
            }
            writer.write(text);

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
