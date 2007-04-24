/*
 * ImportPublish.java
 *
 * Created on 12 March 2007, 15:38
 *
 */

package no.schibstedsok.searchportal.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;

/**
 * General support to import page fragments from publishing system.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class ImportPublish {
    
    // Constants -----------------------------------------------------
    
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    public static void importPage(
            final String page, 
            final DataModel datamodel, 
            final Writer out) throws IOException{
        
        final Properties props = datamodel.getSite().getSiteConfiguration().getProperties();

        final URL u = new URL(props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_URL) + page + ".html");
        final String hostHeader = props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_HOST);
        
        final HTTPClient client = HTTPClient.instance(u.getHost(), u.getPort(), hostHeader);
        final BufferedReader reader = client.getBufferedReader(u.getPath());

        try{
            for(String line = reader.readLine();line!=null;line=reader.readLine()){
                out.write(line);
                out.write('\n');
            }
        }catch(IOException ioe){
            throw client.interceptIOException(ioe);
        }
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
