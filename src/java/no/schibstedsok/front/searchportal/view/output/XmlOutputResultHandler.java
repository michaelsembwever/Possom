/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.view.output;

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
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XmlOutputResultHandler implements ResultHandler {

    private static Log log = LogFactory.getLog(XmlOutputResultHandler.class);

    public XmlOutputResultHandler() {
    }

    public void handleResult(final Context cxt, final Map parameters) {

        final SearchResult result = cxt.getSearchResult();

        String[] xmlParam = (String[]) parameters.get("xml");

        if (xmlParam != null && xmlParam[0].equals("yes")) {
            HttpServletRequest request = (HttpServletRequest) parameters
                    .get("request");
            HttpServletResponse response = (HttpServletResponse) parameters
                    .get("response");

            if (request == null || response == null) {
                throw new IllegalStateException(
                        "Both request and response must be set in the parameters");
            }

            // PrintWriter from a Servlet
            try {
                PrintWriter out = response.getWriter();
                StreamResult streamResult = new StreamResult(out);
                SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
                        .newInstance();
                // SAX2.0 ContentHandler.
                TransformerHandler hd = tf.newTransformerHandler();
                Transformer serializer = hd.getTransformer();
                serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");

                hd.setResult(streamResult);
                hd.startDocument();
                AttributesImpl atts = new AttributesImpl();

                atts.addAttribute("", "hits", "hits", "", String.valueOf(result.getHitCount()));

                hd.startElement("", "", "searchResult", atts);

                Attributes emptyAtts = new AttributesImpl();

                 for (Iterator iter = result.getResults().iterator(); iter.hasNext();) {
                    SearchResultItem item = (SearchResultItem) iter.next();
                    hd.startElement("", "", "resultItem", emptyAtts);
                    for (Iterator iterator = item.getFieldNames().iterator(); iterator.hasNext();) {
                        String field = (String) iterator.next();

                        log.debug("field name is " + field);

                        Object fieldValue = item.getFieldAsObject(field);


                        if (fieldValue != null) {

                            hd.startElement("", "", field, emptyAtts);
                            if (!(fieldValue instanceof ArrayList)) {
                                hd.characters(fieldValue.toString().toCharArray(), 0, fieldValue.toString().length());
                            } else {
                                Collection valueArray = (Collection) fieldValue;

                                for (Iterator valueIterator = valueArray.iterator(); valueIterator.hasNext();) {
                                    String singleValue = (String) valueIterator.next();
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
                log.error("Error", e);
            } catch (IllegalArgumentException e) {
                // FIXME
                log.error("Error", e);
            } catch (IOException e) {
                // FIXME
                log.error("Error", e);
            } catch (TransformerFactoryConfigurationError e) {
                // FIXME
                log.error("Error", e);
            } catch (SAXException e) {
                // FIXME
                log.error("Error", e);
            }
        }

        }}
