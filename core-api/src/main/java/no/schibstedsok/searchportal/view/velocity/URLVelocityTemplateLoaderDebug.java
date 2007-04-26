package no.schibstedsok.searchportal.view.velocity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * UrlVelocityTemplateLoaderDebug adds border around every
 * velocity template that is loaded. This is a nice way to
 * see wich templates are loaded and where they are loaded.
 * 
 * @author Ola M Sagli <a mailto="ola@sesam.no">ola@sesam.no</a>
 *
 */
public class URLVelocityTemplateLoaderDebug extends URLVelocityTemplateLoader {
	
	private static Logger log = Logger.getLogger(URLVelocityTemplateLoaderDebug.class);
	static String HTML_DIV = "<div style=\"margin: 3px; border: 1px solid #C0C0C0\">\n";
	static int NOT_MATCH = -1;
	
	private String header(String content) {
		return "<div style=\"background:#DDDDDD; font-size:10px\">" + content + "</div>\n";
	}

	/**
	 * getResourceStream() loads resource from url. Then add frame and display velocity filename 
	 * before returning the content. 
	 */
	@Override
	public InputStream getResourceStream(String url) throws ResourceNotFoundException {
		log.info("getResourceStream() " + url);

		String 	showBorder = System.getProperty("VELOCITY_DEBUG_BORDER");
		
		if("false".equals(showBorder)) {
			return super.getResourceStream(url);
		}
		
		String relUrl = url.replaceAll("http://(.*?)/", "/");		

		InputStream stream = null;
		String base = System.getProperty("VELOCITY_DEBUG_TEMPLATES");
		
		if(base == null) {
			stream = super.getResourceStream(url);
		} else {
			try {
				relUrl = relUrl.replace("localhost", "");
				File file = new File(base + relUrl);
				log.info("Load template from " + file.getAbsolutePath()+ "(exist =" + file.exists() + ")");				
				if(file.exists()) {
				
					stream = new FileInputStream(new File(base + relUrl));
				}else {
					stream = super.getResourceStream(url);
				}
			} catch (FileNotFoundException ignore) {
				System.err.println("Error: " + ignore);
			}
		}
		if(url.indexOf("rss") != -1) {
			return stream;
		}
		
		StringBuffer buffer = new StringBuffer();
		try {

        	byte b[] = new byte[stream.available()];
        	stream.read(b);
        	stream.close();
          	String content = new String(b);
          	
        	if(content.indexOf("<content") != NOT_MATCH) {
        		String output = content.toString().replaceAll("<content(.*?)>", 
        				"<content $1>\n"  + HTML_DIV + " " + header(url) + " $1 ");
        		output = output.replaceAll("</content>", "</div>\n</content>");
        		return new ByteArrayInputStream(output.getBytes());
        	}
        			// means new templatestyle
        	buffer.append(HTML_DIV);      
        	buffer.append(header(relUrl));
        	buffer.append(content);
        	buffer.append("  \n</div>\n");
        	//log.info("Result: " + buffer.toString());
        	return new ByteArrayInputStream(buffer.toString().getBytes());
        	
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
