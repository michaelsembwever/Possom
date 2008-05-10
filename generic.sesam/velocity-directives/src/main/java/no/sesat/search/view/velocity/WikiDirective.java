/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.view.velocity;

import java.net.URLEncoder;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.Writer;
import java.io.IOException;

/**
 *
 * A velocity directive to convert wiki url
 *
 * <code>
 * #wiki('http://no.wikipedia.org/wiki/Planet')
 * returns the string: "http://no.wapedia.org/Planet"
 * </code>
 *
 *
 *
 */
public class WikiDirective extends Directive {

    private static final String NAME = "wiki";

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
    public boolean render(
            final InternalContextAdapter context,
            final Writer writer, final
            Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        final String s = node.jjtGetChild(0).value(context).toString();
        final String wap;
        if(s.contains("no.wikipedia.org/wiki"))
        {
        	wap = s.replace("no.wikipedia.org/wiki", "no.wapedia.org");
        }
        else
        {
        	wap = s.replace("sv.wikipedia.org/wiki", "sv.wapedia.org");
        }

        String cut = wap.substring(0, wap.lastIndexOf("/")+1);
        String wikiword = URLEncoder.encode(s.substring(s.lastIndexOf("/")+1), "UTF-8");
        writer.write(cut+wikiword);

//        writer.write(StringEscapeUtils.escapeXml(wap));

        if (node.getLastToken().image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;

    }
}
