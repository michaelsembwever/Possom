// Copyright (2006) Schibsted Søk AS
/*
 * CapitalizeWordsDirective.java
 *
 * Created on February 13, 2006, 3:45 PM
 *
 */

package no.schibstedsok.front.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang.WordUtils;
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
 * A velocity directive to capitalize word(s)
 *
 * <code>
 * #capitalizeWords('thomas kjerstad')
 * </code>
 *
 * The default charset is utf-8.
 *
 * @author thomas
 */
public final class CapitalizeWordsDirective extends Directive {

    private static final Logger LOG = Logger.getLogger(CapitalizeWordsDirective.class);

    private static final String NAME = "capitalizeWords";

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
        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        final String input = node.jjtGetChild(0).value(context).toString();

        writer.write(WordUtils.capitalize(input));

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }
}
