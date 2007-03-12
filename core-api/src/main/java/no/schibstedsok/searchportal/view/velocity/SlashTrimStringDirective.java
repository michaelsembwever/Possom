// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.view.velocity;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.log4j.Logger;

import java.io.Writer;
import java.io.IOException;

/**
 * Directive used to trim everytinhg from the start of the string to the last slash.
 * primary used to remove starting navigators in subnavigator names
 * User: ant
 * Date: 12.mar.2007
 * Time: 10:09:44
 */
public class SlashTrimStringDirective extends Directive {
    private static final Logger LOG = Logger.getLogger(ChopStringDirective.class);
    private static final String NAME = "slashTrimString";

    public String getName() {
        return NAME;
    }

    public int getType() {
        return LINE;
    }

    public boolean render(InternalContextAdapter internalContextAdapter,
                          Writer writer,
                          Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - Wrong number of arguments");
            return false;
        }

         final Object nodeValue = node.jjtGetChild(0).value(internalContextAdapter);
           if(nodeValue == null) {
                // No need to do anything since the string is empty anyway
                writer.write("");
                return true;

            }
        final String originalString = nodeValue.toString();
        final int index = originalString.lastIndexOf("/");
        if(index == -1)
        {
            writer.write(originalString);
        }
        else{
            writer.write(originalString.substring(index+1,originalString.length()));
        }
        return true;
    }
}
