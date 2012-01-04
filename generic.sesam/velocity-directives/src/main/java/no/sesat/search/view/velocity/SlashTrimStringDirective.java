/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.view.velocity;

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
        String trimAfter = "/";

        if (node.jjtGetNumChildren() == 2) {
           final Object nodeTwo = node.jjtGetChild(1).value(internalContextAdapter);
           if(nodeTwo != null) {
            trimAfter =  nodeTwo.toString();
           }
        }else if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - Wrong number of arguments");
            return false;
        }

         final Object nodeValue = node.jjtGetChild(0).value(internalContextAdapter);
           if(nodeValue == null) {
                // No need to do anything since the string is empty anyway
                writer.write("");
                return true;

            }
        String originalString = nodeValue.toString();
        int index = originalString.lastIndexOf(trimAfter);
         // trim away trailing separator if it exists
        if(index == originalString.length() -1)
        {
           originalString = originalString.substring(0,originalString.length() -1);
           index = originalString.lastIndexOf(trimAfter);
        }
        if(index == -1)
        {
            writer.write(originalString);
        }
        else{
            writer.write(originalString.substring(index+trimAfter.length(),originalString.length()));
        }
        return true;
    }
}
