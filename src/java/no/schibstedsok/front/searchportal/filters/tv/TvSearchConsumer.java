/**
 * 
 */
package no.schibstedsok.front.searchportal.filters.tv;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.tv.service.TvFeed;
import no.schibstedsok.tv.service.TvFeedImpl;
import no.schibstedsok.tv.service.TvSearchResultImpl;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * A TV SearchConsumer.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class TvSearchConsumer extends SearchConsumer {


	Logger log = Logger.getLogger(this.getClass());
	
	//thread notification
	private boolean available;
	
    /**
	 * Create a new TVSearchConsumer.
	 * 
	 * @param response
	 * @param configuration
	 */
	public TvSearchConsumer(ServletResponse response, SearchConfiguration configuration) {
		super(response, configuration);
	}

    /**
	 * Create a new TVSearchConsumer.
	 * 
	 * @param response
	 * @param configuration
	 */
	public TvSearchConsumer(Writer response, SearchConfiguration configuration) {
		super(response, configuration);
	}

	/* (non-Javadoc)
     * @see com.schibstedsok.portal.search.filters.SearchConsumer#run()
     */
    public synchronized void run() {
        {
			SearchThread w = null;
			if(myResponseRef != null) {
				w = new SearchThread(myResponseRef, configuration);
			} else {				
				w = new SearchThread(myWriterRef, configuration);
			}
			
            final Thread thread = new Thread(w);

            thread.start();
			long start = System.currentTimeMillis();
            try {
                while(!available){
					wait(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
				log.error("interrupted!");
            } 
        }
		int i = 0;
    }

    /**
     * 
     * Inner class implementing the TV search
     * 
     * 
     * @author Lars Johansson
     *
     */
    public class SearchThread implements Runnable {

		Logger log = Logger.getLogger(this.getClass());
		
		private SearchConfiguration configuration;

        public SearchThread(ServletResponse response, SearchConfiguration config) {
            myResponseRef = response;
			configuration = config;
        }

        public SearchThread(Writer response, SearchConfiguration config) {
            myWriterRef = response;
			configuration = config;
        }

        public void run() {
			
			
            try {
				long start = System.currentTimeMillis();
				TvFeed bean = new TvFeedImpl();
				TvSearchResultImpl result = bean.search(configuration.getQuery());
				print(result, configuration.getTemplate());
				if(log.isDebugEnabled())
					log.debug("TV search took: " + (System.currentTimeMillis() - start) + " msec.");

			} finally {

				// clean up all references
				myResponseRef = null;
				myWriterRef = null;

				//signal completion to thread waiting
				available = true;
			}
        }

		/** 
		 * 
		 * @param searchResponse
		 * @param template
		 */
		private void print(TvSearchResultImpl searchResponse, String template) {
			printResults(searchResponse, template);
			
		}

		/** 
		 * 
		 * Prints a standard FAST result including timing info and documents returned.
		 * 
		 * @param searchResponse
		 * @param string
		 * @throws IOException 
		 */
		private void printResults(TvSearchResultImpl searchResponse, String template) {

			if (myResponseRef != null) {
				try {
					printToServletResponse(searchResponse, myResponseRef, template);
				} catch (IOException e) {
					log.error("Error", e);
				}
			} else {
				printVelocityToWriter(searchResponse, myWriterRef, template);
			}
		}

		private void printResults(Collection results, String template) {
			
			if (myResponseRef != null) {
				try {
					printToServletResponse(results, myResponseRef, template);
				} catch (IOException e) {
					log.error("Error", e);
					throw new RuntimeException();
				}
			} else {
				printVelocityToWriter(results, myWriterRef, template);
			}
		}

		private void printVelocityToWriter(Object results, Writer myWriterRef, String templateName) {

//			initVelocity(templateName);

			try {
                Template template = Velocity.getTemplate(templateName);
				VelocityContext context = new VelocityContext();
				context.put("result", results);
				template.merge(context, myWriterRef);
				if(log.isDebugEnabled())
					log.debug("Merged Velocity-template: " + templateName);
				
			} catch (Exception e1) {
				// TODO: handle this exception
				e1.printStackTrace();
			}
			
		}

		private void printToServletResponse(Object results, ServletResponse responseRef, String template) throws IOException {

			printVelocityToWriter(results, responseRef.getWriter(), template);
	        responseRef.getWriter().flush();
		}

    }

}

