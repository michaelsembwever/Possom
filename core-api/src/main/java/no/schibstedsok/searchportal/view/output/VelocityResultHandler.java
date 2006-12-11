// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.view.output;

import java.io.IOException;
import java.util.Collections;
import no.geodata.maputil.CoordHelper;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.result.Decoder;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.view.velocity.VelocityEngineFactory;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.util.PagingDisplayHelper;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.DateTool;

import java.io.StringWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.net.URLEncoder;
import java.util.Properties;

/** Handles the populating the velocity contexts.
 * Strictly view domain.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 * 
 * @deprecated Please use the DataModelResultHandler instead.
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
    private static final String ERR_NP_WRITING_TO_STREAM 
            = "Possible client cancelled request. (NullPointerException writing to response's stream).";
    private static final String ERR_MERGE = "Error merging template ";

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
        
        // site
        context.put(Site.NAME_KEY, site);
        // coord helper
        context.put("coordHelper", new CoordHelper());
        // decoder
        context.put("decoder", new Decoder());
        // math tool
        context.put("math", new MathTool());
        // date tool
        context.put("date", new DateTool());
        return context;
    }

    public void handleResult(final Context cxt, final Map parameters) {

        // Skip this result handler if xml is wanted.
        final String[] xmlParam = (String[]) parameters.get("xml");
        if (xmlParam != null && xmlParam[0].equals("yes")) {
            return;
        }

        LOG.trace("handleResult()");

        // write to a separate writer first for threading reasons
        final Writer w = new StringWriter();
        final SearchConfiguration searchConfiguration 
                = cxt.getSearchResult().getSearchCommand().getSearchConfiguration();
        
            final String templateName = searchConfiguration.getName() + ".vm";

            LOG.debug(DEBUG_TEMPLATE_SEARCH + searchConfiguration + templateName);

            final Site site = cxt.getSite();
            final VelocityEngine engine = VelocityEngineFactory.valueOf(
                    ContextWrapper.wrap(VelocityEngineFactory.Context.class, cxt)).getEngine();
            final Template template = getTemplate(engine, site, searchConfiguration.getName());

            if(template != null){

                LOG.debug(DEBUG_TEMPLATE_FOUND + template.getName());

                final VelocityContext context = newContextInstance(engine);
                populateVelocityContext(context, cxt, parameters);

                try {

                    template.merge(context, w);
                    writeToResponse(parameters, w.toString());

                } catch (MethodInvocationException ex) {
                    LOG.error("Exception for reference: " + ex.getReferenceName());
                    throw new InfrastructureException(ERR_MERGE + templateName, ex);

                } catch (ResourceNotFoundException ex) {
                    throw new InfrastructureException(ERR_MERGE + templateName, ex);

                } catch (ParseErrorException ex) {
                    throw new InfrastructureException(ERR_MERGE + templateName, ex);

                } catch (IOException ex) {
                    throw new InfrastructureException(ERR_MERGE + templateName, ex);

                } catch (NullPointerException ex) {
                    //at com.opensymphony.module.sitemesh.filter.RoutablePrintWriter.write(RoutablePrintWriter.java:132)

                    // indicates an error in the underlying RoutablePrintWriter stream
                    //  typically the client has closed the connection
                    LOG.warn(ERR_NP_WRITING_TO_STREAM);

                } catch (Exception ex) {
                    throw new InfrastructureException(ERR_MERGE + templateName, ex);

                }

            }else{
                LOG.error(ERR_TEMPLATE_NOT_FOUND + templateName);
                throw new UnsupportedOperationException(ERR_TEMPLATE_NOT_FOUND + templateName);
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

        // push all parameters into velocity context attributes. make a copy first as others could be updating it.
        for(Map.Entry<String,Object> entry : Collections.unmodifiableSet(parameters.entrySet())){
            /* do not overwrite parameters already in the velocity context */
            if (!context.containsKey(entry.getKey())) {
                // don't put back in String array that only contains one element
                context.put(entry.getKey(), entry.getValue() instanceof String[] && ((String[])entry.getValue()).length ==1
                        ? context.put(entry.getKey(), ((String[])entry.getValue())[0])
                        : entry.getValue());
            }
        }

        // populate context with request and response // TODO remove, since all attributes are copied in
        context.put("request", parameters.get("request"));
        context.put("response", parameters.get("response"));

        // search-portal attributes
        context.put("result", cxt.getSearchResult());
        context.put("query", queryStringURLEncoded);
        context.put("queryHTMLEscaped", queryString);
        context.put("currentTab", cxt.getSearchTab()); // FIXME duplicate of "tab"

        // following are deprecated as the view domain should not be accessing them
        context.put("globalSearchTips", ((RunningQuery) parameters.get("query")).getGlobalSearchTips());
        context.put("runningQuery", (RunningQuery) parameters.get("query"));
        context.put("command", cxt.getSearchResult().getSearchCommand());
        
        // TODO remove. deprecated since the template can access the configuration property directly now.
        final Properties props = (Properties)parameters.get("configuration");
        context.put(PUBLISH_URL, props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_URL));
        context.put(PUBLISH_HOST, props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_HOST));
        

        final SearchConfiguration config = cxt.getSearchResult().getSearchCommand().getSearchConfiguration();

        if (config.isPaging()) {
            final PagingDisplayHelper pager = new PagingDisplayHelper(
                    cxt.getSearchResult().getHitCount(),
                    config.getResultsToReturn(),
                    cxt.getSearchTab().getPageSize());

            final Object v = parameters.get("offset");
            pager.setCurrentOffset(Integer.parseInt(v instanceof String[]
                    ? ((String[]) v)[0]
                    : (String) v));
            pager.setPageSize(cxt.getSearchTab().getPageSize());
            
            context.put("pager", pager);
        }
    }
    
    /** HACKS around having to import javax.servlet stuff into the core-api.
     * Also handles tests by not writing at all.
     * */
    private void writeToResponse(final Map<String,Object> parameters, final String string){
        
        try {
            final Object object = parameters.get("response");
            if( null != object ){
                final Method method = object.getClass().getMethod("getWriter", new Class[]{});
                if( null != method ){
                    final Writer writer = (Writer) method.invoke(object, new Object[]{});
                    writer.write(string);
                }
            }
            
            
        }catch (InvocationTargetException ex) {
            LOG.error(ex.getMessage(), ex);
        }catch (IllegalAccessException ex) {
            LOG.error(ex.getMessage(), ex);
        }catch (IllegalArgumentException ex) {
            LOG.error(ex.getMessage(), ex);
        }catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }catch (NoSuchMethodException ex) {
            LOG.error(ex.getMessage(), ex);
        }catch (SecurityException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
    
}
