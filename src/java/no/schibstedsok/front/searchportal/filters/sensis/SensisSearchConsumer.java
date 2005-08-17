/**
 * 
 */
package no.schibstedsok.front.searchportal.filters.sensis;

import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.util.Collection;

import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.command.ConnectorCommand;
import no.schibstedsok.front.searchportal.command.SensisConnectorCommand;
import no.schibstedsok.front.searchportal.connectors.FastConnector;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.response.FastSearchResponseImpl;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @author Lars Johansson
 *
 */
public class SensisSearchConsumer extends SearchConsumer {


    private boolean available;
	Logger log = Logger.getLogger(this.getClass());


    /**
	 * Create a new SensisSearchConsumer.
	 * 
	 * @param response
	 * @param configuration
	 */
	public SensisSearchConsumer(ServletResponse response, SearchConfiguration configuration) {
		super(response, configuration);
//		log.debug("sat up SeachConsumer with configuration template " + configuration.getTemplate());		
	}
	
    /**
	 * Create a new SensisSearchConsumer.
	 * 
	 * @param response
	 * @param configuration
	 */
	public SensisSearchConsumer(Writer response, SearchConfiguration configuration) {
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
			
            /** TODO: Not sure if we need to wait for completion? */
			try {
                while(!available){
					wait(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
				log.info("SearchThread interrupted");
            } 
        }
    }
    /**
     * 
     * Inner class implementing the Sensis (Fast) search
     * 
     * 
     * @author Lars Johansson
     *
     */
    public class SearchThread implements Runnable {

        ConnectorCommand sensisCommand = new SensisConnectorCommand();

        ServletResponse myResponseRef = null;

		Logger log = Logger.getLogger(this.getClass());

		
        public SearchThread(ServletResponse response, SearchConfiguration config) {
            myResponseRef = response;
			configuration= config;
        }

        public SearchThread(Writer response, SearchConfiguration config) {
            myWriterRef = response;
			configuration = config;
        }

        public void run() {
            try {

				// connect and execute search!
				FastConnector connector = (FastConnector)FastConnector.getInstance();
				connector.execute(sensisCommand, configuration);

				FastSearchResponseImpl searchResponse = (FastSearchResponseImpl) sensisCommand.getResponse();
				try {
					printResults(searchResponse, configuration.getTemplate());

				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {

				// clean up all references
				sensisCommand = null;
				myResponseRef = null;
				myWriterRef = null;
				
				//signal completion to thread waiting
				available = true;

			}
        }

		/** 
		 * 
		 * Prints Sensis result
		 * 
		 * @param searchResponse
		 * @param string
		 * @throws IOException 
		 */
		private void printResults(FastSearchResponseImpl searchResponse, String template) throws IOException {

			if (myResponseRef != null) {
				printToServletResponse(searchResponse, myResponseRef, template);
			} else {
				printVelocityToWriter(searchResponse, myWriterRef, template);
			}
			
		}

		private void printResults(Collection results, String template) throws IOException {
			
			if (myResponseRef != null) {
				printToServletResponse(results, myResponseRef, template);
			} else {
				printVelocityToWriter(results, myWriterRef, template);
			}
		}

		private void printVelocityToWriter(Object results, Writer myWriterRef, String templateName) {
			        
//			initVelocity(templateName);
			
			try {

                Writer w = new StringWriter();

                Template template = Velocity.getTemplate(templateName);
				VelocityContext context = new VelocityContext();
				context.put("result", results);
				template.merge(context, w);
				if(log.isDebugEnabled())
						log.debug("Merged Velocity-template: " + templateName);
                w.close();
                myWriterRef.write(w.toString());

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

