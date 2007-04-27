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
 * UrlVelocityTemplateLoaderDebug adds border around every velocity template
 * that is loaded. This is a nice way to see wich templates are loaded and where
 * they are loaded.
 * 
 * @author Ola M Sagli <a mailto="ola@sesam.no">ola@sesam.no</a>
 * 
 */
public class URLVelocityTemplateLoader extends URLResourceLoader {

	static String DIV_TAG = "div";
	static String STYLE_ATTRIB = "style";
	static String STYLE_BORDER = "margin:3px;border:1px solid #C0C0C0";
	static String STYLE_HEADING="background:#DDDDDD;font-size:10px";

	/**
	 * getResourceStream() loads resource from url. Then add border around the
	 * template so its easy to see wich templates are loaded.
	 */
	public InputStream getResourceStreamDebug(String url)
			throws ResourceNotFoundException {

		boolean VELOCITY_DEBUG = "true".equals(System.getProperty("VELOCITY_DEBUG"));

		if (!VELOCITY_DEBUG) {
			return super.getResourceStream(url);
		}
		InputStream stream = null;
		String filePath = url.replaceAll("http://(.*?)/", "/").replace("locahost", "");
		String templatesDir = System.getProperty("VELOCITY_TEMPLATES_DIR");

		stream = getStream(templatesDir, filePath, url);
			// If rss, means the output is xml. 
		if (url.indexOf("rss") != -1) {
			return stream;
		}

		byte byteContent[];
		try {
			byteContent = new byte[stream.available()];
			stream.read(byteContent);
			stream.close();
		} catch (IOException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}

		// Create html 
		String template= new String(byteContent);
		StringWriter writer = new StringWriter();
		Document doc = createDocument();
		
		Element border = doc.createElement(DIV_TAG);
		border.setAttribute(STYLE_ATTRIB, STYLE_BORDER);
			
		Element divHeader = doc.createElement(DIV_TAG);
		divHeader.setAttribute(STYLE_ATTRIB, STYLE_HEADING);
		divHeader.appendChild(doc.createTextNode(filePath));
		border.appendChild(divHeader);
		
		border.appendChild(doc.createCDATASection(template));
		doc.appendChild(border);

		internalWriteDocument(doc, writer);
		
		String result = writer.getBuffer().toString();
		result = result.replace("<![CDATA[", "");
		result = result.replace("]]>", "");
		System.out.println("Tamplate =" + result);
			// log.info("Result: " + buffer.toString());
		return new ByteArrayInputStream(result.getBytes());
	}

	/*
	 * Get stream from file of or url.
	 */
	private InputStream getStream(String templatesDir, String filePath,String url) {
		
		System.out.println("*** Loading: " + templatesDir + filePath + ", or url: " + url);

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
