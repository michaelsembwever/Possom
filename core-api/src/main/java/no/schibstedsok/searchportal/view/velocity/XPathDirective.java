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

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.Writer;
import java.io.IOException;

/**
 * Evaluates xpath expression and writes the result as a string. Expects the following velocity parameters:
 * <ul>
 * <li>the expression</li>
 * <li>optional org.w3c.dom.Node to apply expression to (default is value of "document" from cxt)</li>
 * </ul>
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
     * Evaluates xpath expression and writes the result as a string. Expects the following velocity parameters:
     * <ul>
     * <li>the expression</li>
     * <li>optional org.w3c.dom.Node to apply expression to (default is value of "document" from cxt)</li>
     * </ul>
     *
     * @param cxt The cxt.
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
    public boolean render(final InternalContextAdapter cxt, final Writer writer, final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (node.jjtGetNumChildren() < 1) {
            LOG.error("#" + getName() + " - missing argument");
            return false;
        }

        try {
            final String expression = node.jjtGetChild(0).value(cxt).toString();
            // Implicitly use doc from cxt if no doc argument was supplied.
            final Object doc = 2 == node.jjtGetNumChildren() ? node.jjtGetChild(1).value(cxt) : cxt.get("document");
            writer.write(XPathFactory.newInstance().newXPath().evaluate(expression, doc));
            return true;
        } catch (XPathExpressionException e) {
            LOG.error(e);
            return false;
        }
    }
}
