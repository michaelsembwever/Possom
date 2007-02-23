/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.view.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public final class XmlOutputResultHandler implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(XmlOutputResultHandler.class);

    /** TODO comment me. **/
    public XmlOutputResultHandler() {
    }

    /** @inherit **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        final SearchResult result = cxt.getSearchResult();

        final Map<String,Object> parameters = datamodel.getJunkYard().getValues();
        final String[] xmlParam = (String[]) parameters.get("xml");

        if (xmlParam != null && xmlParam[0].equals("yes")) {
            final HttpServletRequest request = (HttpServletRequest) parameters
                    .get("request");
            final HttpServletResponse response = (HttpServletResponse) parameters
                    .get("response");

            if (request == null || response == null) {
                throw new IllegalStateException(
                        "Both request and response must be set in the parameters");
            }

            // PrintWriter from a Servlet
            try {
                final PrintWriter out = response.getWriter();
                final StreamResult streamResult = new StreamResult(out);
                final SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
                        .newInstance();
                // SAX2.0 ContentHandler.
                final TransformerHandler hd = tf.newTransformerHandler();
                final Transformer serializer = hd.getTransformer();
                serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");

                hd.setResult(streamResult);
                hd.startDocument();
                final AttributesImpl atts = new AttributesImpl();

                atts.addAttribute("", "hits", "hits", "", String.valueOf(result.getHitCount()));

                hd.startElement("", "", "searchResult", atts);

                final Attributes emptyAtts = new AttributesImpl();

                 for (final Iterator iter = result.getResults().iterator(); iter.hasNext();) {
                    final SearchResultItem item = (SearchResultItem) iter.next();
                    hd.startElement("", "", "resultItem", emptyAtts);
                    for (final Iterator iterator = item.getFieldNames().iterator(); iterator.hasNext();) {
                        final String field = (String) iterator.next();

                        LOG.debug("field name is " + field);

                        final Object fieldValue = item.getFieldAsObject(field);


                        if (fieldValue != null) {

                            hd.startElement("", "", field, emptyAtts);
                            if (!(fieldValue instanceof ArrayList)) {
                                hd.characters(fieldValue.toString().toCharArray(), 0, fieldValue.toString().length());
                            } else {
                                final Collection valueArray = (Collection) fieldValue;

                                for (final Iterator valueIterator = valueArray.iterator(); valueIterator.hasNext();) {
                                    final String singleValue = (String) valueIterator.next();
                                    hd.startElement("", "", "value", emptyAtts);
                                    hd.characters(singleValue.toCharArray(), 0, singleValue.length());
                                    hd.endElement("", "", "value");
                                }

                            }
                            hd.endElement("", "", field);
                        }
                    }
                    hd.endElement("", "", "resultItem");
                 }

                hd.endElement("", "", "searchResult");
                hd.endDocument();

                out.flush();
            } catch (TransformerConfigurationException e) {
                // FIXME
                LOG.error("Error", e);
            } catch (IllegalArgumentException e) {
                // FIXME
                LOG.error("Error", e);
            } catch (IOException e) {
                // FIXME
                LOG.error("Error", e);
            } catch (TransformerFactoryConfigurationError e) {
                // FIXME
                LOG.error("Error", e);
            } catch (SAXException e) {
                // FIXME
                LOG.error("Error", e);
            }
        }

        }}
