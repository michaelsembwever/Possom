/* Copyright (2007-2009) Schibsted ASA
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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
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
 * - independant word ("Billån" should not match lån)
 *
 * <code>
 * #boldWord('leter du etter hotell i Paris' 'hotell i paris')
 * returns the string: "leter du etter <b>hotell</b> i <b>Paris</b>"
 * </code>
 *
 *
 *
 * @version $Id$
 */
public final class BoldWordDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(BoldWordDirective.class);

    private static final String NAME = "boldWord";

    public String getName() {
        return NAME;
    }

    public int getType() {
        return LINE;
    }

    public boolean render(
                final InternalContextAdapter context,
                final Writer writer,
                final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        final int argCount = node.jjtGetNumChildren();

        if (argCount != 1) {

            String text = getArgument(context, node, 0);
            final String uquery = getArgument(context, node, 1);
            String query = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(uquery);

            if(text == null) {
                writer.write("");
                return true;
            }
            query = query.replaceAll("\"", "").replaceAll("'", "");

            final List list = Arrays.asList(query.split("[\\p{Punct}\\p{Space}]+"));

            for (int i=0;i<list.size();i++) {
                if (!list.get(i).toString().toLowerCase().equals("og") && !list.get(i).toString().toLowerCase().equals("i")) {

                    final String regexPattern = "(\\s|^)("+list.get(i).toString()+")(?![a-z])";
                    final Pattern p = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
                    final Matcher m = p.matcher(text);
                    text = m.replaceAll(" <b>$2</b>");
                }
            }
            writer.write(text);

            if (node.getLastToken().image.endsWith("\n")) {
                writer.write('\n');
            }

        }else{

            final String msg = '#' + getName() + " - wrong number of arguments";
            LOG.error(msg);
            return false;
        }

        return true;

    }
}
