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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class VelocityDebugServlet extends HttpServlet{
	
	static String VELOCITY_DEBUG = "VELOCITY_DEBUG";
	static String HTML = "html";
	static String BODY = "body";
	static String HEADER = "h2";
 	static String ON_OFF = "Velocityborder is ";

	static {
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
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
