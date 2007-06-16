/*
 * ImportPublish.java
 *
 * Created on 12 March 2007, 15:38
 *
 */

package no.schibstedsok.searchportal.view;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * General support to import page fragments from publishing system.
 * Caches content on a one minute basis to reduce outbound socket connections.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class ImportPublish {
    
    // Constants -----------------------------------------------------

    private static final GeneralCacheAdministrator CACHE = new GeneralCacheAdministrator();   
    private static final int REFRESH_PERIOD = 60; // one minute
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    /**
     * 
     * @param page 
     * @param datamodel 
     * @param out 
     * @throws java.io.IOException 
     */
    public static String importPage(
            final String page, 
            final DataModel datamodel) throws IOException{
        
        final Properties props = datamodel.getSite().getSiteConfiguration().getProperties();

        final URL u = new URL(props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_URL) + page + ".html");
        final String hostHeader = props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_HOST);
        final String cacheKey = '[' + hostHeader + ']' + u.toString();
        
        String content = "";
        try{
            content = (String) CACHE.getFromCache(cacheKey, REFRESH_PERIOD);
        
        } catch (NeedsRefreshException nre) {
        
            boolean updatedCache = false;
            final HTTPClient client = HTTPClient.instance(hostHeader, u);
            
            try{
                final BufferedReader reader = client.getBufferedReader("");
                final StringBuilder builder = new StringBuilder();
          
                for(String line = reader.readLine(); line != null; line = reader.readLine()){
                    builder.append(line);
                    builder.append('\n');
                }
                content = builder.toString();
                CACHE.putInCache(cacheKey, content);
                updatedCache = true;

            }catch(IOException ioe){
                content = (String) nre.getCacheContent();
                throw client.interceptIOException(ioe);
                
            }finally{
                if(!updatedCache){ 
                    CACHE.cancelUpdate(cacheKey);
                }
            }
        }
        return content;
    }
         
    /**
     * 
     * @param page 
     * @param datamodel 
     * @param out 
     * @throws java.io.IOException 
     */
    public static Document importXml(
            final String page, 
            final DataModel datamodel) throws IOException, ParserConfigurationException, SAXException{ 
        
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(importPage(page, datamodel));
       
    }   
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of NewClass */
    private ImportPublish() {
    }
    
    // Public --------------------------------------------------------
    
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}
