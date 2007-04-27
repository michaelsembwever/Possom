package no.schibstedsok.searchportal.view.velocity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

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
import org.apache.velocity.exception.ResourceNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * Load templates and adds debuginformation if VELOCITY_DEBUG is set to true.
 * 
 * @see URLResourceLoader
 * 
 * 
 * @author Ola M Sagli <a mailto="ola@sesam.no">ola@sesam.no</a>
 * 
 */
public class URLVelocityTemplateLoader extends URLResourceLoader {

	private static final String DIV_TAG = "div";
	private static final String STYLE_ATTRIB = "style";
	private static final String STYLE_BORDER = "margin:3px;border:1px solid #C0C0C0";
	private static final String STYLE_HEADING="background:#DDDDDD;font-size:10px";

	/**
	 * getResourceStream() loads resource from url. Then add border around the
	 * template so its easy to see wich templates are loaded.
	 */
	@Override
	public InputStream getResourceStream(String url)
			throws ResourceNotFoundException {

		boolean VELOCITY_DEBUG = "true".equals(System.getProperty("VELOCITY_DEBUG"));
		boolean VELOCITY_DEBUG_ON ="true".equals(System.getProperty("VELOCITY_DEBUG_ON"));
		
		if(!(VELOCITY_DEBUG && VELOCITY_DEBUG_ON)) {
			return super.getResourceStream(url);
		}
		
		InputStream stream = null;
		String filePath = url.replaceAll("http://(.*?)/", "/").replace("locahost", "");
		String templatesDir = System.getProperty("VELOCITY_TEMPLATES_DIR");

		if(filePath.endsWith("head.vm")) {
			return super.getResourceStream(url);
		}
		stream = getStream(templatesDir, filePath, url);
			// If rss, means the output is xml. 
		if (url.indexOf("rss") != -1) {
			return stream;
		}

		StringBuffer streamBuffer = new StringBuffer();

		try {
			int c;
			while((c = stream.read()) != -1) {
				streamBuffer.append((char)c);
			}
		} catch (IOException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}

		// Create html 
		StringBuffer template= new  StringBuffer();
		template.append("\n");
		template.append(streamBuffer);
		template.append("\n");

		StringWriter writer = new StringWriter();
		Document doc = createDocument();
		
		Element border = doc.createElement(DIV_TAG);
		border.setAttribute(STYLE_ATTRIB, STYLE_BORDER);
			
		Element divHeader = doc.createElement(DIV_TAG);
		divHeader.setAttribute(STYLE_ATTRIB, STYLE_HEADING);
		divHeader.appendChild(doc.createTextNode(filePath));
		border.appendChild(divHeader);
		
		border.appendChild(doc.createCDATASection(template.toString()));
		doc.appendChild(border);

		internalWriteDocument(doc, writer);
	
		String result = writer.getBuffer().toString();
		result = result.replace("<![CDATA[", "");
		result = result.replace("]]>", "");
		//System.out.println("*** Result: " + result.toString());
		//System.out.println("*** Result.length: " + result.length());
		return new ByteArrayInputStream(result.getBytes());
	}

	/*
	 * Get stream from file of or url.
	 */
	private InputStream getStream(String templatesDir, String filePath,String url) {

		if(templatesDir == null) {
			return super.getResourceStream(url);
		}
		
		File file = new File(templatesDir + filePath);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException ignore) {
				return super.getResourceStream(url);
			}
		} else {
			return super.getResourceStream(url);
		}
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
