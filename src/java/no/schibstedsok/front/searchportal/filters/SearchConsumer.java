/**
 * 
 */
package no.schibstedsok.front.searchportal.filters;

import java.io.Writer;

import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.util.SearchConfiguration;

import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;

/**
 * 
 * A base Class for SearchConsumer.
 * 
 * Threaded consumer of search queries with references to a writer it may print 
 * results on.
 * 
 * @author Lars Johansson
 * 
 */
public abstract class SearchConsumer implements Runnable {

    protected ServletResponse myResponseRef = null;		// ResponseObject used to print information on
    protected Writer myWriterRef = null;				// basic Writer instead of ServletResponse
    protected SearchConfiguration configuration = null;

	Logger log = Logger.getLogger(this.getClass());

	/**
	 * 
	 * Create a new SearchConsumer.
	 * @deprecated
	 * 
	 */
	public SearchConsumer(){
	}
	
    public SearchConsumer(ServletResponse response, SearchConfiguration config) {
        this.myResponseRef = response;
		this.configuration = config;
    }
   
    public SearchConsumer(Writer response, SearchConfiguration config) {
        this.myWriterRef = response;
		this.configuration = config;
    }
   
	/** override this method in your specif implementation of SearchConsumer */
    public abstract void run();

	
	
    /** trim long URLs down to 60 characters */
	protected String trimClickUrl(String url){
        
        if(url== null)
            return url;
        
        if(url.length() > 60)
            url = url.substring(0,60) + "...";
        
        return url;

    }
	
	/**
	 *  Init Velocit Singleton.
	 * 
	 * @param templateName
	 */
	public void _initVelocity(String templateName) {
		if(!Velocity.resourceExists(templateName)){
            try {
                Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
                Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
				Velocity.init();
			} catch (Exception e) {
				log.error("Error", e);
			}				
		}
	}
	
 }
