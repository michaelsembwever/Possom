// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.view.output;

import java.io.IOException;
import no.geodata.maputil.CoordHelper;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SiteConfiguration;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.result.Decoder;
import no.schibstedsok.front.searchportal.result.Linkpulse;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.view.velocity.VelocityEngineFactory;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.util.Channels;
import no.schibstedsok.front.searchportal.util.PagingDisplayHelper;
import no.schibstedsok.front.searchportal.util.TradeDoubler;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.MathTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.net.URLEncoder;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;

/** Handles the populating the velocity contexts.
 * Strictly view domain.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class VelocityResultHandler implements ResultHandler {

    private static final String PUBLISH_URL = "publishSystemBaseURL";
    private static final String PUBLISH_HOST = "publishSystemHostHeader";

    private static final Logger LOG = Logger.getLogger(VelocityResultHandler.class);

    private static final String INFO_TEMPLATE_NOT_FOUND = "Could not find template ";
    private static final String ERR_IN_TEMPLATE = "Error parsing template ";
    private static final String ERR_GETTING_TEMPLATE = "Error getting template ";
    private static final String DEBUG_TEMPLATE_SEARCH = "Looking for template ";
    private static final String DEBUG_TEMPLATE_FOUND = "Created template ";
    private static final String ERR_TEMPLATE_NOT_FOUND = "Could not find the template ";
    private static final String ERR_NP_WRITING_TO_STREAM = "Possible client cancelled request. (NullPointerException writing to response's stream).";

    public static VelocityEngine getEngine(final Context cxt){

        return VelocityEngineFactory.valueOf(ContextWrapper.wrap(VelocityEngineFactory.Context.class,cxt)).getEngine();
    }
    
    /** Utility wrapper to getEngine(Context) defaulting to UrlResourceLoader resource loading. **/
    public static VelocityEngine getEngine(final Site site){

        return VelocityEngineFactory.valueOf(new VelocityEngineFactory.Context(){
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return UrlResourceLoader.newDocumentLoader(this, resource, builder);
            }
            public Site getSite() {
                return site;
            }
        }).getEngine();
    }

    public static Template getTemplate(
            final VelocityEngine engine,
            final Site site,
            final String templateName){

        final String templateUrl = site.getTemplateDir() + "/" + templateName + ".vm";
        try {
            return  engine.getTemplate(templateUrl);

        } catch (ResourceNotFoundException ex) {
            // expected possible behaviour
            LOG.debug(INFO_TEMPLATE_NOT_FOUND + templateUrl);

        } catch (ParseErrorException ex) {
            LOG.error(ERR_IN_TEMPLATE + templateUrl, ex);
            throw new InfrastructureException(ex);

        } catch (Exception ex) {
            LOG.error(ERR_GETTING_TEMPLATE + templateUrl, ex);
            throw new InfrastructureException(ex);
        }
        return null;
    }

    public static VelocityContext newContextInstance(final VelocityEngine engine){
        final VelocityContext context = new VelocityContext();
        final Site site = (Site) engine.getProperty(Site.NAME_KEY);
        final Site fallbackSite = (Site) engine.getProperty("site.fallback");
        // site
        context.put(Site.NAME_KEY, site);
        context.put("fallbackSite", fallbackSite);
        context.put("locale", site.getLocale());
        // publishing system
        context.put(PUBLISH_URL, engine.getProperty(SearchConstants.PUBLISH_SYSTEM_URL));
        context.put(PUBLISH_HOST, engine.getProperty(SearchConstants.PUBLISH_SYSTEM_HOST));
        // coord helper
        context.put("coordHelper", new CoordHelper());
        // properties
        context.put("linkpulse", new Linkpulse(site, SiteConfiguration.valueOf(site).getProperties()));
        // decoder
        context.put("decoder", new Decoder());
        // math tool
        context.put("math", new MathTool());
        return context;
    }

    public void handleResult(final Context cxt, final Map parameters) {

        // Skip this result handler if xml is wanted.
        final String[] xmlParam = (String[]) parameters.get("xml");
        if (xmlParam != null && xmlParam[0].equals("yes")) {
            return;
        }

        LOG.trace("handleResult()");

        // This requirement of the users of this class to send the web stuff
        // as parameters is a bit too implicit...
        final HttpServletRequest request = (HttpServletRequest) parameters.get("request");
        final HttpServletResponse response = (HttpServletResponse) parameters.get("response");
        if (request == null || response == null) {
            throw new IllegalStateException("Both request and response must be set in the parameters");
        }

        // write to a separate writer first for threading reasons
        final Writer w = new StringWriter();
        final SearchConfiguration searchConfiguration = cxt.getSearchResult().getSearchCommand().getSearchConfiguration();

            LOG.debug(DEBUG_TEMPLATE_SEARCH + searchConfiguration + searchConfiguration.getName() + ".vm");

            final Site site = cxt.getSite();
            final VelocityEngine engine = getEngine(cxt);
            final Template template = getTemplate(engine, site, searchConfiguration.getName());

            if( template != null ){
            
                LOG.debug(DEBUG_TEMPLATE_FOUND + template.getName());

                final VelocityContext context = newContextInstance(engine);
                populateVelocityContext(context, cxt, parameters);

                try {

                    template.merge(context, w);
                    response.getWriter().write(w.toString());

                } catch (MethodInvocationException ex) {
                    throw new InfrastructureException(ex);

                } catch (ResourceNotFoundException ex) {
                    throw new InfrastructureException(ex);

                } catch (ParseErrorException ex) {
                    throw new InfrastructureException(ex);

                } catch (IOException ex) {
                    throw new InfrastructureException(ex);

                } catch (NullPointerException ex) {
                    //at com.opensymphony.module.sitemesh.filter.RoutablePrintWriter.write(RoutablePrintWriter.java:132)

                    // indicates an error in the underlying RoutablePrintWriter stream
                    //  typically the client has closed the connection
                    LOG.warn(ERR_NP_WRITING_TO_STREAM);

                } catch (Exception ex) {
                    throw new InfrastructureException(ex);

                }
            
            }else{
                LOG.error(ERR_TEMPLATE_NOT_FOUND + searchConfiguration.getName() + ".vm");
                throw new UnsupportedOperationException(ERR_TEMPLATE_NOT_FOUND + searchConfiguration.getName() + ".vm");
            }

    }

    protected void populateVelocityContext(final VelocityContext context,
                                           final Context cxt,
                                           final Map<String,Object> parameters) {

        LOG.trace("populateVelocityContext()");

        String queryString = cxt.getQuery().getQueryString();

        String queryStringURLEncoded = null;

        try {
            queryStringURLEncoded = URLEncoder.encode(queryString, "UTF-8");
            queryString = StringEscapeUtils.escapeHtml(queryString);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }
        
        // push all parameters into request attributes
        for( Map.Entry<String,Object> entry : parameters.entrySet() ){
            // don't put back in String array that only contains one element
            context.put(entry.getKey(), entry.getValue() instanceof String[] && ((String[])entry.getValue()).length ==1
                    ? context.put(entry.getKey(), ((String[])entry.getValue())[0])
                    : entry.getValue());
        }
        
        // populate context with request and response // TODO remove, since all attributes are copied in
        final HttpServletRequest request = (HttpServletRequest) parameters.get("request");
        final HttpServletResponse response = (HttpServletResponse) parameters.get("response");
        context.put("request", request);
        context.put("response", response);

        // search-portal attributes
        context.put("result", cxt.getSearchResult());
        context.put("query", queryStringURLEncoded);
        context.put("queryHTMLEscaped", queryString);
        context.put("currentTab", cxt.getSearchTab()); // duplicate of "tab"
        context.put("contextPath", request.getContextPath());
        context.put("tradedoubler", new TradeDoubler(request));
        
        // following are deprecated as the view domain should not be accessing them
        context.put("globalSearchTips", ((RunningQuery) request.getAttribute("query")).getGlobalSearchTips());
        context.put("runningQuery", (RunningQuery) request.getAttribute("query"));
        context.put("command", cxt.getSearchResult().getSearchCommand());

        /* TODO: check where this went */
        /* context.put("text", TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class,cxt))); */
        context.put("channels", Channels.valueOf(ContextWrapper.wrap(Channels.Context.class, cxt)));

        final SearchConfiguration config = cxt.getSearchResult().getSearchCommand().getSearchConfiguration();

        if (config.isPagingEnabled()) {
            final PagingDisplayHelper pager = new PagingDisplayHelper(
                    cxt.getSearchResult().getHitCount(), 
                    config.getResultsToReturn(), 
                    cxt.getSearchTab().getPageSize());
            
            final Object v = parameters.get("offset");
            pager.setCurrentOffset(Integer.parseInt( v instanceof String[] 
                    ? ((String[]) v)[0]
                    : (String) v));
            
            context.put("pager", pager);
        }


    }
}
