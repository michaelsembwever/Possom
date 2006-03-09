package no.schibstedsok.front.searchportal.velocity;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.WordUtils;

import java.io.Writer;
import java.io.IOException;

/**
 *
 * A velocity directive to chop a string
 *
 * <code>
 * #chopString('this string is being chopped!' 20)
 * returns the string: "this string is.."
 * </code>
 *
 *
 * @author thomas
 */
public class ChopStringDirective extends Directive {

    private static transient Log log = LogFactory.getLog(ChopStringDirective.class);


    private static final String NAME = "chopString";

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
     * Renders the capitalized word(s).
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
        if (node.jjtGetNumChildren() != 2) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        String s = node.jjtGetChild(0).value(context).toString();
        int length = Integer.parseInt(node.jjtGetChild(1).value(context).toString());

        String choppedString = "";
        if (s.length() <= length)
            choppedString = s;
        else {
            String sub = s.substring(0, length);
            String lastChar = Character.toString(sub.charAt(sub.length() - 1));
            if (lastChar.equals("."))
                choppedString = sub.substring(0, length) + "..";
            else if (lastChar.equals(" "))
                choppedString = sub.substring(0, length) + " ...";
            else {
		int lastSpace = sub.lastIndexOf(" ");

		if (lastSpace >= 0) {
		    choppedString = sub.substring(0, sub.lastIndexOf(" ")) + " ...";
		} else {
		    choppedString = sub.substring(0, length) + "...";
		}
	    }
        }

        writer.write(choppedString);

        Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;

    }
}
