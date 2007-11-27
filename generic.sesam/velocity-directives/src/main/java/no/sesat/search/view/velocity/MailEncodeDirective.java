/*
 * Copyright (2005-2007) Schibsted Søk AS
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

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * A velocity directive to encode mail addresses to prevent crawling.
 * Uses Javascript to decode the coded string.
 *
 * @author <a href="mailto:endre@sesam.no">Endre Midtgård Meckelborg</a>
 * @version <tt>$Revision: $</tt>
 */
public final class MailEncodeDirective extends Directive {

    private static final Logger LOG = Logger.getLogger(MailEncodeDirective.class);

    private static final String NAME = "mailEncode";

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
            throws IOException {
        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
        } else {
            final String input = (null != node.jjtGetChild(0).value(context)
                ? node.jjtGetChild(0).value(context).toString() : "");

            // Do the encoding.
            final StringBuilder output = new StringBuilder();

            for (char c : input.toCharArray()) {
                if (output.length() > 0) {
                    output.append(",");
                }
                output.append((int) c);
            }

            writer.write(output.toString());

            final Token lastToken = node.getLastToken();

            if (lastToken.image.endsWith("\n")) {
                writer.write("\n");
            }

            return true;
        }
        return false;
    }

}
