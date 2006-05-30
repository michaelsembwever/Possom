package no.schibstedsok.front.searchportal.view.velocity;

import java.io.IOException;

import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 */
public final class ChopStringDirective extends Directive {

    private static transient Log log = LogFactory.getLog(ChopStringDirective.class);

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
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() != 2 && node.jjtGetNumChildren() != 3) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        final String s = node.jjtGetChild(0).value(context).toString();
        final int length = Integer.parseInt(node.jjtGetChild(1).value(context).toString());


        String choppedString = "";
        if (s.length() <= length)
            choppedString = s;
        else {
            final String sub = s.substring(0, length);
            final String lastChar = Character.toString(sub.charAt(sub.length() - 1));
            if (lastChar.equals("."))
                choppedString = sub.substring(0, length) + "..";
            else if (lastChar.equals(" "))
                choppedString = sub.substring(0, length) + " ...";
            else {
		        final int lastSpace = sub.lastIndexOf(" ");

                if (lastSpace >= 0) {
                    choppedString = sub.substring(0, sub.lastIndexOf(" ")) + " ...";
                } else {
                    choppedString = sub.substring(0, length) + "...";
                }
	        }
        }

        if (node.jjtGetNumChildren() > 2) {
            String htmlescape = node.jjtGetChild(2).value(context).toString();
            if (htmlescape.equals("esc"))
                writer.write(StringEscapeUtils.escapeHtml(choppedString));
            else
                writer.write(choppedString);
        } else
            writer.write(choppedString);

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;

    }
}
