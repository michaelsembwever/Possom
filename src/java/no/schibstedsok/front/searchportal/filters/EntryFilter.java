package no.schibstedsok.front.searchportal.filters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.analyzer.Analyzer;
import no.schibstedsok.front.searchportal.analyzer.AnalyzerEngine;
import no.schibstedsok.front.searchportal.filters.fast.WebCrawlFilter;
import no.schibstedsok.front.searchportal.filters.fast.MediaCollectionFilter;
import no.schibstedsok.front.searchportal.filters.fast.WikiFilter;
import no.schibstedsok.front.searchportal.filters.fast.CompaniesFilter;
import no.schibstedsok.front.searchportal.filters.sensis.SensisFilter;
import no.schibstedsok.front.searchportal.util.ProcessList;
import no.schibstedsok.front.searchportal.util.SearchConstants;

import org.apache.velocity.app.Velocity;

/**
 * <p>
 * TODO: Describe this better
 * 
 * Filter that will intercept requests and....
 * 
 * <p>
 * Install the filter in your web.xml file as follows:
 * 
 * <pre>
 * 
 *  
 *  &lt;filter&gt;
 *    &lt;filter-name&gt;Entry Filter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;com.schibstedsok.portal.search.filters.EntryFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *    &lt;!-- specify ... the filter should use --&gt;
 *      &lt;!-- if not specified - defaults to &quot;X&quot; --&gt;
 *      &lt;param-name&gt;activate.param&lt;/param-name&gt;
 *      &lt;param-value&gt;app_profile&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *      &lt;!-- specify the whether to start the filter automatically --&gt;
 *      &lt;!-- if not specified - defaults to &quot;true&quot; --&gt;
 *      &lt;param-name&gt;autostart&lt;/param-name&gt;
 *      &lt;param-value&gt;false&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * &lt;/filter&gt;  
 *  
 * </pre>
 * 
 * <p>
 * With the above settings you can turn the filter on by accessing any URL with
 * the parameter <code>profilingfilter=on</code>.eg:
 * 
 * <pre>
 * 
 *      http://mywebsite.com/a.jsp?&lt;b&gt;&lt;i&gt;profilingfilter=on&lt;/i&gt;&lt;/b&gt;
 *  
 * </pre>
 * 
 * <p>
 * The above settings also sets the filter to not start automatically upon
 * startup. This may be useful for production, but you will most likely want to
 * set this true in development.
 * 
 * 
 * @author Lars Johansson
 * 
 */

public final class EntryFilter extends BaseFilter {

    /** shared Sitemesh header template across instances */
	private static String headerTemplate = null;

	public void doExecute(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        ThreadGroup threadGroup = new ThreadGroup("search_group" + Thread.currentThread().getName());

        request.setAttribute("threadGroup", threadGroup);

        /** timer */
		long start = System.currentTimeMillis();

		/** Get default pipeline that allwasy execute */
		List requestPipeLine = new ArrayList(ProcessList.getInstance().getProcessList());

		/** Textual analyzis looks at query parameters and add filters to pipeline*/
		doTextanalyzis(request, requestPipeLine);

		/**
		 * Navigational analyzis is responsible for targeted search in collections/indexes.
		 * Adds suitable filter to pipeline.
		 **/
		doNavigationAnalyzis(request, requestPipeLine);

		filterConfig.getServletContext().log("Executing pipeline: " + requestPipeLine);
		request.setAttribute("processList", requestPipeLine);

		/** prepare the reponse and Sitemesh headers */
		response.setContentType("text/html");
        response.getWriter().write(getHeaderTemplate());

        chain.doFilter(request, response);

		waitForCompletion(threadGroup);

		response.getWriter().write("(c) 2005 </body></html>");
        response.flushBuffer();
        threadGroup.destroy();
    }

	private void doTextanalyzis(ServletRequest request, List pipeLine) {
		Analyzer analyzer = AnalyzerEngine.getAnalyzer();
		List additionalFilters = analyzer.analyze(request.getParameter(SearchConstants.REQUEST_KEYPARAM_QUERY));
		if(pipeLine.addAll(additionalFilters))
			filterConfig.getServletContext().log("Added filter(s) to pipeline");
	}

	/**
	 *
	 * Basic implementation of navigation in collections.
	 * Look at the request parameter to decide which filter to invoke.
	 *
	 * @param request
	 * @param pipeLine
	 */
	private void doNavigationAnalyzis(ServletRequest request, List pipeLine) {

		/** see if there is a collection param */
		if(request.getParameter(SearchConstants.REQUEST_KEYPARAM_COLLECTION) != null){

			/** Handle multiple collections a'la c=d&c=m etc. 	 */
			String[] collections = request.getParameterValues(SearchConstants.REQUEST_KEYPARAM_COLLECTION);
			if(collections.length == 0) {
				return;
			}

			for (int i = 0; i < collections.length; i++) {
				pipeLine.add(filterMatch(collections[i]));
			}
		}
	}

	/**
	 * Check if the collection is valid/defined.
	 *
	 * TODO: refactor into a map or something.
	 *
	 * @param string
	 * @return
	 */
	private String filterMatch(String string) {

		if(string.equals(SearchConstants.REQUEST_PARAM_DEFAULT_COLLECTIONS))
			return WebCrawlFilter.class.getName();
		if(string.equals(SearchConstants.REQUEST_PARAM_MEDIA_COLLECTIONS))
			return MediaCollectionFilter.class.getName();
		if(string.equals(SearchConstants.REQUEST_PARAM_WIKICOLLECTION))
			return WikiFilter.class.getName();
		if(string.equals(SearchConstants.REQUEST_PARAM_GLOBAL_INDEX))
			return SensisFilter.class.getName();
        if (string.equals(SearchConstants.REQUEST_PARAM_COMPANIES_INDEX))
            return CompaniesFilter.class.getName();
        else return null;
	}

	/**
	 *
	 */
	private void waitForCompletion(ThreadGroup group) {

        Thread threads[] = new Thread[group.activeCount()];

        group.enumerate(threads);

        for (int i = 0; i < threads.length; i++) {
            Thread thread = threads[i];
            try {
                if (thread != null) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    private void setupHeaderTemplate() {
       getHeaderTemplate();
    }

    private String getHeaderTemplate(){
        
        if(headerTemplate == null) {
            StringBuffer header = new StringBuffer();
            InputStream headerStream = getClass().getResourceAsStream("/searchheader.html");
            if(headerStream != null){
                int i;
                try {
                    while((i = headerStream.read()) != -1){
                        header.append((char)i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                headerTemplate = header.toString();
            } else {
                /** For Sitemesh we must set up the header, so we have a fallback to default! */
				headerTemplate = "<html><head><meta name=\"decorator\" content=\"maindecorator\" /><link href=\"../css/style.css\" rel=\"stylesheet\" type=\"text/css\" /></head><body>";
				filterConfig.getServletContext().log("Could not find headerTemplate using default!");
            }
        } 
        
        return headerTemplate;
        
    }
	
	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);
		
		/** initialize Sitemesh header */
		setupHeaderTemplate();
		
		/** initialize all text analyzers */
		AnalyzerEngine.getAnalyzer().analyze("");
		
		/** initialize Velocity */
		initVelocity();
		
	}
	
	/**
	 *  Init the Velocity Singleton.
	 * 
	 */
	public void initVelocity() {
		
		try {
			
			Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
			Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			Velocity.init();

			filterConfig.getServletContext().log("Initialized Velocity for application");
			
		} catch (Exception e) {
			filterConfig.getServletContext().log("Velocity error ", e);
		}				
	}

}
