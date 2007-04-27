package no.schibstedsok.searchportal.http.servlet;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Servlet for switcing velocitydebug on/off
 * @author ola <a mailto="ola@sesam.no"> ola@sesam.no </a>
 *
 */
public class VelocityDebugServlet extends HttpServlet{
	
	private static final Logger LOG = Logger.getLogger(VelocityDebugServlet.class);
	/* Key for switching velocitydebug on/off */
	private static final String VELOCITY_DEBUG = "VELOCITY_DEBUG";
	/* Html tag */
	private static final String HTML = "html";
	/* Body tag */
	private static final String BODY = "body";
	/* Header tag */
	private static final String HEADER = "h2";
	/* Message to user */
 	private static final String ON_OFF = "Velocityborder is ";

	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		if(!isLocalhost(request)) {
			LOG.warn("velocitydebug when running localhost only.");
			return;
		}
		// TODO Auto-generated method stub
		String velocityDebug = System.getProperty(VELOCITY_DEBUG);
		String debugStatus = "false";
		
		if("true".equals(velocityDebug)) {
			debugStatus = "false";
		}else{
			debugStatus = "true";
		}
		
		System.setProperty(VELOCITY_DEBUG, debugStatus);		

		Document doc = createDocument();
		
		Element html = doc.createElement(HTML);
		Element body = doc.createElement(BODY);
		
		body.appendChild(doc.createTextNode(ON_OFF + " " + debugStatus));
		
		html.appendChild(body);
		doc.appendChild(html);

		internalWriteDocument(doc, response.getWriter());
		
	}

	/*
	 * Assure that debug is only used when running localhost
	 */
	private boolean isLocalhost(HttpServletRequest request) {
		String ipAddr = (String)request.getAttribute("REMOTE_ADDR") + "";
        return ipAddr.startsWith("127.") || ipAddr.startsWith("10.") || ipAddr.startsWith("0:0:0:0:0:0:0:1%0");
	}
	
    // -- Write the document to the writer
    private void internalWriteDocument(Document d, Writer w) {
        DOMSource source = new DOMSource(d);
        StreamResult result = new StreamResult(w);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Xml Parser: " + e);
        } catch (TransformerException ignore) {
        }
    }
    
    // -- Create a DOM document
    private Document createDocument() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder builder = null;
        try {
            builder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc = builder.newDocument();
        return doc;
    }
}
