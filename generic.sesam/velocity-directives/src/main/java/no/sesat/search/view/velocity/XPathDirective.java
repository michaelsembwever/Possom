/* Copyright (2006-2007) Schibsted ASA
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
/*
 * XPath.java
 *
 */
package no.sesat.search.view.velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.log4j.Logger;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.Writer;
import java.io.IOException;

/**
 * Evaluates xpath expression and writes the result as a string. Expects the following velocity parameters:
 * <ul>
 * <li>optional variable to assign result to</li>
 * <li>the expression</li>
 * <li>org.w3c.dom.Node to apply expression to (default is value of "document" from cxt)</li>
 * </ul>
 *
 *
 */
public final class XPathDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(XPath.class);

    private static final String NAME = "xpath";

    private String targetVariable;

    public void init(final RuntimeServices runtimeServices, final InternalContextAdapter context, final Node node)
            throws TemplateInitException {
        super.init(runtimeServices, context, node);

        if (node.jjtGetNumChildren() > 2) {

            final SimpleNode sn = (SimpleNode) node.jjtGetChild(0);

            if (sn instanceof ASTReference) {
                targetVariable = ((ASTReference) sn).getRootString();
            } else {
                targetVariable = sn.getFirstToken().image.substring(1);
            }
        }
    }

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
     * <li>optional target variable</li>
     * <li>the expression</li>
     * <li>optional if target variable was not supplied org.w3c.dom.Node to apply expression to (default is value of
     * "document" from cxt)</li>
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

            int nextArg = targetVariable == null ? 0 : 1;

            final String expression = getArgument(cxt, node, nextArg++);

            // Implicitly use doc from cxt if no doc argument was supplied.
            final Object doc = node.jjtGetNumChildren() > 1
                    ? node.jjtGetChild(nextArg).value(cxt)
                    : cxt.get("document");

            final String xPathResult = XPathFactory.newInstance().newXPath().evaluate(expression, doc);

            if (targetVariable == null) {
                writer.write(xPathResult);
            } else {
                cxt.put(targetVariable, xPathResult);
            }

            return true;
        } catch (XPathExpressionException e) {
            LOG.error(e);
            return false;
        }
    }
}
