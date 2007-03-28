/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.view.velocity;

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
