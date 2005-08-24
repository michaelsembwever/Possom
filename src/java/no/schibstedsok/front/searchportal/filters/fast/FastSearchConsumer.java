/**
 * 
 */
package no.schibstedsok.front.searchportal.filters.fast;

import java.io.*;
import java.util.Collection;

import javax.servlet.ServletResponse;

import no.fast.ds.search.SearchEngineException;
import no.schibstedsok.front.searchportal.command.FastConnectorCommand;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.connectors.FastConnector;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.response.FastSearchResponseImpl;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * A FastSearchConsumer.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class FastSearchConsumer extends SearchConsumer {


	Logger log = Logger.getLogger(this.getClass());
	
	//thread notification
	private boolean available;
	
    /**
	 * Create a new FastSearchConsumer.
	 * 
	 * @param response
	 * @param configuration
	 */
	public FastSearchConsumer(ServletResponse response, SearchConfiguration configuration) {
		super(response, configuration);
	}

    /**
	 * Create a new FastSearchConsumer.
	 * 
	 * @param response
	 * @param configuration
	 */
	public FastSearchConsumer(Writer response, SearchConfiguration configuration) {
		super(response, (FastSearchConfiguration)configuration);
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
     * Inner class implementing the Fast search
     * 
     * 
     * @author Lars Johansson
     *
     */
    public class SearchThread implements Runnable {

		Logger log = Logger.getLogger(this.getClass());
		
        FastConnectorCommand fastCommand = new FastConnectorCommand();

        ServletResponse myResponseRef = null;
		
		private int maxResults;

		private String collection = null;

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

				// connect and execute search!
				FastConnector connector = (FastConnector)FastConnector.getInstance();
				connector.execute(fastCommand, configuration);

				FastSearchResponseImpl searchResponse = (FastSearchResponseImpl) fastCommand.getResponse();
				print(searchResponse, configuration.getTemplate());

			} finally {
				// clean up all references
				fastCommand = null;
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
		private void print(FastSearchResponseImpl searchResponse, String template) {
			printResults(searchResponse, template);

//			/** print result to debug output */
//			if(log.isDebugEnabled()){
//				Iterator iter = searchResponse.getAllResults().iterator();
//				while (iter.hasNext()) {
//					SearchResultElement element = (SearchResultElement) iter.next();
//					log.debug(element.getTitle());
//				}
//			}

		}

//		private void printAll(FastSearchResponseImpl searchResponse) throws IOException {
//			printWiki(searchResponse);
//			printMedia(searchResponse);
//			printWebCrawl(searchResponse);
//		}
//
//		private void printWebCrawl(FastSearchResponseImpl searchResponse) throws IOException {
//			printResults(searchResponse, SearchConstants.WEBCRAWL_TEMPLATE_FILE);
//		}
//
//		private void printMedia(FastSearchResponseImpl searchResponse) throws IOException {
//			if (searchResponse.getRetrieverResults() != null && searchResponse.getRetrieverResults().size() > 0) {
//				printResults(searchResponse.getRetrieverResults(), SearchConstants.RETRIEVER_TEMPLATE_FILE);
//			}
//		}
//
//		private void printWiki(FastSearchResponseImpl searchResponse) throws IOException {
//			if (searchResponse.getWikiResult() != null && searchResponse.getWikiResult().size() > 0) {
//				printResults(searchResponse.getWikiResult(), SearchConstants.WIKI_TEMPLATE_FILE);

//			}
//		}

		/** 
		 * 
		 * Prints a standard FAST result including timing info and documents returned.
		 * 
		 * @param searchResponse
		 * @param string
		 * @throws IOException 
		 */
		private void printResults(FastSearchResponseImpl searchResponse, String template) {

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

			initVelocity();

			try {

                Writer w = new StringWriter();

                Template template = Velocity.getTemplate(templateName);
				
				VelocityContext context = new VelocityContext();
				context.put("result", results);
				template.merge(context, w);
				log.debug("Merged template: " + templateName);

                w.close();

                myWriterRef.write(w.toString());

            } catch (Exception e1) {
				e1.printStackTrace();
			}
			
		}

		/** 
		 * make sure Velocity have been initialized.
		 */
		private void initVelocity() {

			/** Ignored if already initialized (makes this method testable.) */
			try {
				Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
				Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
				Velocity.init();
			} catch (Exception e) {
				// FIXME
				log.error("Error", e);
			}

			
		}

		private void printToServletResponse(Object results, ServletResponse responseRef, String template) throws IOException {
			printVelocityToWriter(results, responseRef.getWriter(), template);
	        responseRef.getWriter().flush();

		}

    }

}

