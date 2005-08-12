package no.schibstedsok.front.searchportal.filters.sensis;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SensisFastSearchConfiguration;
import no.schibstedsok.front.searchportal.filters.AsynchronusBaseFilter;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;

/**
 * 
 * This is a prototype Filter, no thread safety etc has been taken into
 * consideration!
 * 
 * @author Lars Johansson
 * 
 */
public final class SensisFilter extends AsynchronusBaseFilter {

	protected FilterConfig filterConfig = null;

	public void doExecuteAsynch(ServletRequest request,
			ServletResponse response, FilterChain chain) throws IOException,
			ServletException {


        filterConfig.getServletContext().log("- Sensis Global - Searching");
		
		FastSearchConfiguration configuration = new SensisFastSearchConfiguration(); 
		configuration = (FastSearchConfiguration)setUpSearchConfiguration(request, (SearchConfiguration)configuration);

		configuration.setLanguage(SearchConstants.LANGUAGE_ENGLISH);
		configuration.setCollection(SearchConstants.DEFAULTCOLLECTION);
		configuration.setTemplate(VelocityTemplates.GLOBAL_SEARCH);

        doSearch(response, configuration, request);
        
        chain.doFilter(request, response);


	}


	/**
	 *
	 * @param response
     * @param configuration
     * @param request
     */
	private void doSearch(ServletResponse response, SearchConfiguration configuration, ServletRequest request) {

		final SearchConsumer w = new SensisSearchConsumer(response, configuration);
        startThread(w, request);

	}

	public void init(FilterConfig filterConfig) {
		super.init(filterConfig);
		this.filterConfig = filterConfig;

		/*
		 * get the velocity properties from the classloader
		 */
		// Properties p = null;
		//
		// try {
		// InputStream is =
		// getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPS);
		// p = new Properties();
		// p.load(is);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// /** load the Sensis velocity template at startup */
		// try {
		//
		// BasicConfigurator.configure();
		// Category log = Category.getInstance("Sensis-filter");
		// Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
		// "org.apache.velocity.runtime.log.SimpleLog4JLogSystem" );
		// Velocity.setProperty("runtime.log.logsystem.log4j.category",
		// "Sensis-filter");
		// Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
		// Velocity.setProperty(Velocity.RESOURCE_MANAGER_LOGWHENFOUND, "true");
		// Velocity.setProperty("class.resource.loader.class",
		// "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		// Velocity.init();
		//
		// } catch (ResourceNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ParseErrorException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
