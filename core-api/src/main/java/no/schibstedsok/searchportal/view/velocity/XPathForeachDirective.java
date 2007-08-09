/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
*
* Jul 18, 2007 4:34:18 PM
*/
package no.schibstedsok.searchportal.view.velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.Writer;
import java.io.IOException;

/**
 * Directive for moving through w3c dom node lists selected by an XPath expression.
 *
 * Example:
 *
 * <blockquote><pre>
 * #publish("/pages/xmlbase/cat.xml")
 * #xpathforeach($item "/categories/item[greatprnt=33]")
 *   #xpath("catname" $item)
 * #end
 * </pre></blockquote>
 *
 * By default, the expression is applied to the value of the context variable "document". If there is a third argument
 * the expression is applied to the value this argument instead. The argument should be a type of
 * <tt>org.w3c.dom.Node</tt>.
 *
 */
public class XPathForeachDirective extends AbstractDirective {

    private static final Logger LOG = Logger.getLogger(XPath.class);

    private String counterName;
    private int counterInitialValue;
    private String elementKey;

    /**
      * {@inheritDoc}
      */
    public void init(RuntimeServices runtimeServices, InternalContextAdapter internalContextAdapter, Node node) throws TemplateInitException {
        super.init(runtimeServices, internalContextAdapter, node);
        counterName = rsvc.getString(RuntimeConstants.COUNTER_NAME);
        counterInitialValue = rsvc.getInt(RuntimeConstants.COUNTER_INITIAL_VALUE);

        final SimpleNode sn = (SimpleNode) node.jjtGetChild(0);

        if (sn instanceof ASTReference) {
            elementKey = ((ASTReference) sn).getRootString();
        } else {
            elementKey = sn.getFirstToken().image.substring(1);
        }
    }

    /**
      * {@inheritDoc}
      */
    public String getName() {
        return "xpathforeach";
    }

    /**
      * {@inheritDoc}
      */
    public int getType() {
        return BLOCK;
    }

    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        final String expression = getArgument(context, node, 1);
        final XPath xPath = XPathFactory.newInstance().newXPath();

        // Save loop variables. Needed to get nested looping to work.
        Object savedElement = context.get(elementKey);
        Object savedCounter = context.get(counterName);

        try {

            final Object doc = node.jjtGetNumChildren() == 4 ? node.jjtGetChild(3) : context.get("document");
            final Object result = xPath.evaluate(expression, doc, XPathConstants.NODESET);

            if (result instanceof NodeList) {
                final NodeList list = (NodeList) result;

                final Node block = node.jjtGetChild(node.jjtGetNumChildren() == 4 ? 3 : 2);

                for (int i = 0; i < list.getLength(); ++i) {
                    context.localPut(elementKey, list.item(i));
                    context.localPut(counterName, i + counterInitialValue);
                    block.render(context, writer);
                }
            }

            return true;
        } catch (XPathExpressionException e) {
            LOG.error(e.getMessage(), e);
            return false;
        } finally {
            // Restore loop variables
            if (savedElement != null) {
                context.put(elementKey, savedElement);
            } else {
                context.remove(elementKey);
            }

            if (savedCounter != null) {
                context.put(counterName, savedCounter);
            } else {
                context.remove(counterName);
            }
        }
    }
}
