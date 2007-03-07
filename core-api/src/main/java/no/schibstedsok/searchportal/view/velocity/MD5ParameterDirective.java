/*
 * Copyright (2005-22007 Schibsted Søk AS
 */
package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.io.Writer;

import no.schibstedsok.searchportal.security.MD5Generator;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * This directive hashes a url parameter so you can build url's with protected parameters.
 *
 * @author <a href="mailto:endre@sesam.no">Endre Midtgård Meckelborg</a>
 * @version <tt>$Revision: $</tt>
 */
public final class MD5ParameterDirective extends Directive {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(MD5ParameterDirective.class);

    /** Name of the directive. */
    private static final String NAME = "md5Parameter";

    /**
     * @return the name of the directive.
     */
    public String getName() {
        return NAME;
    }

    /**
     * @return the type of the directive. The type is LINE.
     */
    public int getType() {
        return LINE;
    }

    /**
     * Renders and returns the hashed text.
     *
     * @param context the directive context
     * @param writer the writer to write to
     * @param node the node to get input from
     * @throws java.io.IOException
     * @throws org.apache.velocity.exception.ResourceNotFoundException
     * @throws org.apache.velocity.exception.ParseErrorException
     * @throws org.apache.velocity.exception.MethodInvocationException
     * @return the hashed parameter
     */
    public boolean render(
            final InternalContextAdapter context,
            final Writer writer,
            final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() != 1) {
            rsvc.error("#" + getName() + " - wrong number of argumants");
            return false;
        }

        final MD5Generator digestGenerator = new MD5Generator("S3SAM rockz");
        final String input = null != node.jjtGetChild(0).value(context)
                ? node.jjtGetChild(0).value(context).toString()
                : "";

        writer.write(digestGenerator.generateMD5(input));

        final Token lastToken = node.getLastToken();
        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }

}
