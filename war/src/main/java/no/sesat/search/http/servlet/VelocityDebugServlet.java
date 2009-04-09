/* Copyright (2007) Schibsted ASA
 *   This file is part of SESAT.
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
package no.sesat.search.http.servlet;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletConfig;
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
 *
 *
 */
public class VelocityDebugServlet extends HttpServlet{

	private static final Logger LOG = Logger.getLogger(VelocityDebugServlet.class);
	/* Key that must be set as system property when starting tomcat */
	private static final String VELOCITY_DEBUG = "VELOCITY_DEBUG";
	/* Key for switching velocitydebug on/off */
	private static final String VELOCITY_DEBUG_ON = "VELOCITY_DEBUG_ON";
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
		Document doc = createDocument();
		Element html = doc.createElement(HTML);
		Element body = doc.createElement(BODY);
		String templateDir = System.getProperty("VELOCITY_DEVELOP_BASEDIR");

		if(!isLocalhost(request) || !"true".equals(System.getProperty(VELOCITY_DEBUG))) {
			LOG.warn("velocitydebug when running localhost and VELOCITY_DEBUG set to true: ipAddr=" + request.getRemoteAddr());
			LOG.warn("your ip is " + request.getRemoteAddr() + " isLocalhost ? " + isLocalhost(request));
			body.appendChild(doc.createTextNode("Localhost only, start with -DVELOCITY_DEBUG=true"));
			html.appendChild(body);
			doc.appendChild(html);
			internalWriteDocument(doc, response.getWriter());
			return;
		}

		// TODO Auto-generated method stub
		String velocityDebug = System.getProperty(VELOCITY_DEBUG_ON);
		String debugStatus = "false";


		if("true".equals(velocityDebug)) {
			debugStatus = "false";
		}else{
			debugStatus = "true";
		}
		System.setProperty(VELOCITY_DEBUG_ON, debugStatus);

		if(templateDir != null) {
			String paths[] = templateDir.split(",");
			for(String path : paths) {
				File file = new File(path);
				body.appendChild(doc.createTextNode("TemplateDir: " + file.getAbsolutePath() + " " + file.exists()));
				body.appendChild(doc.createElement("BR"));
			}
		}
		//if(request.getQueryString() != null) {
		//	response.sendRedirect("/search/?" + request.getQueryString());
		//}

		body.appendChild(doc.createTextNode(ON_OFF + " " + debugStatus));
		body.appendChild(doc.createElement("BR"));

		html.appendChild(body);
		doc.appendChild(html);
		internalWriteDocument(doc, response.getWriter());
	}

	/*
	 * Assure that debug is only used when running localhost
	 */
	private boolean isLocalhost(HttpServletRequest request) {
		String ipAddr = (String)request.getAttribute("REMOTE_ADDR");
		if(ipAddr == null) {
			ipAddr = request.getRemoteAddr() + "";
		}
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
