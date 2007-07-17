// Copyright (2006-2007) Schibsted Søk AS
/*
 * XPath.java
 *
 */
package no.schibstedsok.searchportal.view.velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.Writer;
import java.io.IOException;

/**
 * Velocity directive to apply xpath expression to a org.w3c.dom.Document.
 *
 * @author magnuse
 */
public final class XPathDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(XPath.class);

    private static final String NAME = "xpath";

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
     * Evaluates xpath expression and adds result to context. Expects the following velocity parameters:
     * <ul>
     * <li>the name of the context variable to hold the result</li>
     * <li>the org.w3c.dom.Document to apply expression to</li>
     * <li>the name of the context variable to hold the result</li>
     * </ul>
     *
     * @param context The context.
     * @param writer The writer.
     * @param node The node.
     *
     * @return return true on success.
     *
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     */
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (node.jjtGetNumChildren() < 1) {
            LOG.error("#" + getName() + " - missing argument");
            return false;
        }

        try {
            final String expression = node.jjtGetChild(0).value(context).toString();

            // Implicitly use document from context if no document argument was supplied.
            final Document document = (Document) (2 == node.jjtGetNumChildren()
                    ? node.jjtGetChild(1).value(context)
                    : context.get("document"));

            writer.write(XPathFactory.newInstance().newXPath().evaluate(expression, document));
            return true;
        } catch (XPathExpressionException e) {
            LOG.error(e);
            return false;
        }
    }
}
