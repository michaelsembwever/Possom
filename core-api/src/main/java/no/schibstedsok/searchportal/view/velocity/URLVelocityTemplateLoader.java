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
	private static final String STYLE_HEADING="text-decoration:underline;font-size:10px";
	private static final String ONMOUSEOVER = "this.style.border='1px solid #C0C0C0';this.style.margin='4px'";
	private static final String ONMOUSEOUT = "this.style.border='none'";
	
	/**
	 * getResourceStream() loads resource from url. Then add border around the
	 * template so its easy to see wich templates are loaded.
	 */
	@Override
	public InputStream getResourceStream(String url)
			throws ResourceNotFoundException {

		boolean VELOCITY_DEBUG = "true".equals(System.getProperty("VELOCITY_DEBUG"));
		boolean VELOCITY_DEBUG_ON ="true".equals(System.getProperty("VELOCITY_DEBUG_ON"));
		boolean STYLE_ONMOUSEOVER ="onmouseover".equals(System.getProperty("VELOCITY_DEBUG_STYLE"));
		boolean STYLE_SILENT ="silent".equals(System.getProperty("VELOCITY_DEBUG_STYLE"));
		
		boolean foundLocal = false;
		
		if(!VELOCITY_DEBUG) {
			return super.getResourceStream(url);
		}
		
		final String templatesDir = System.getProperty("VELOCITY_DEVELOP_BASEDIR");		
		InputStream stream = null;

		String filePath = url.replaceAll("http://(.*?)/", "/").replace("localhost/", "");

		File file = getFile(templatesDir, filePath);

		if(file.exists()) {
			foundLocal = true;
			stream = getStream(file);
		}else{
			stream = super.getResourceStream(url);
		}

		if(!VELOCITY_DEBUG_ON) {
			return stream;
		}
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
		
		Element div = doc.createElement(DIV_TAG);
		
		if(STYLE_ONMOUSEOVER) {
			div.setAttribute("onmouseover", ONMOUSEOVER);
			div.setAttribute("onmouseout", ONMOUSEOUT);
		}else if(STYLE_SILENT){
				// Just print title as popup.
		}else {
			Element divHeader = doc.createElement(DIV_TAG);
			divHeader.setAttribute(STYLE_ATTRIB, STYLE_HEADING);
			divHeader.appendChild(doc.createTextNode(filePath));
			div.appendChild(divHeader);
			div.setAttribute("style", STYLE_BORDER);
		}
		
		div.setAttribute("title", file.getName() + (foundLocal ? "(Editable)" : "(Not editable)"));		
		div.appendChild(doc.createCDATASection(template.toString()));
		doc.appendChild(div);

		internalWriteDocument(doc, writer);
	
		String result = writer.getBuffer().toString();
		result = result.replace("<![CDATA[", "");
		result = result.replace("]]>", "");

		return new ByteArrayInputStream(result.getBytes());
	}

	/*
	 * Create file object
	 */
	private File getFile(String templatesDir, String filePath) {
		if(templatesDir == null) {
			return new File("null" + filePath);
		}
		String paths[] = templatesDir.split(",");
		
		for(String p : paths) {
			File file = new File(p + filePath);
			if(file.exists()) {
				return file;
			}
		}
		return new File(filePath);
	}

	/*
	 * Get stream from file of or url.
	 */
	private InputStream getStream(File file) {

		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException ignore) {
				throw new RuntimeException("File exist but filenotfoundexception thrown: " + ignore);
			}
		} else {
			throw new IllegalArgumentException("File does not exist");
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
