// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.view.output;

import java.io.IOException;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.view.velocity.VelocityEngineFactory;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.util.PagingDisplayHelper;
import no.schibstedsok.searchportal.util.Channels;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import java.io.StringWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import no.schibstedsok.searchportal.util.Channel;

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

    private static final String DEBUG_TEMPLATE_SEARCH = "Looking for template ";
    private static final String DEBUG_TEMPLATE_FOUND = "Created template ";
    private static final String ERR_TEMPLATE_NOT_FOUND = "Could not find the template ";
    private static final String ERR_NP_WRITING_TO_STREAM 
            = "Possible client cancelled request. (NullPointerException writing to response's stream).";
    private static final String ERR_MERGE = "Error merging template ";
    
    /* This is the paging size when browsing resultset like <- 1 2 3 4 5 6 7 8 9 10 ->  Hardcoded to max 10 and independent of the  pageSize */
    // TODO Put this as parameter in views.xml  
    private static final int PAGING_SIZE =  10;

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

            try{
                
                final Template template 
                        = VelocityEngineFactory.getTemplate(engine, site, searchConfiguration.getName());
            
                LOG.debug(DEBUG_TEMPLATE_FOUND + template.getName());

                final VelocityContext context = VelocityEngineFactory.newContextInstance(engine);
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

            }catch(ResourceNotFoundException rnfe){
                LOG.error(ERR_TEMPLATE_NOT_FOUND + templateName);
                LOG.error("Configuration: " + searchConfiguration.getName() + " " + searchConfiguration.getStatisticalName());
                throw new InfrastructureException(ERR_TEMPLATE_NOT_FOUND + templateName, rnfe);
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
        final Set<Map.Entry<String,Object>> set  = new HashSet<Map.Entry<String,Object>>(parameters.entrySet());
        for(Map.Entry<String,Object> entry : set){
            
            /* do not overwrite parameters already in the velocity context */
            if (!context.containsKey(entry.getKey())) {
                
                // don't put back in String array that only contains one element
                context.put(entry.getKey(), 
                        entry.getValue() instanceof String[] && ((String[])entry.getValue()).length ==1
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

        /* TODO: check where this went */
        /* context.put("text", TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class,cxt))); */
        context.put("channels", Channels.valueOf(ContextWrapper.wrap(Channels.Context.class, cxt)));
        
        context.put("channelCategories", Channel.Category.values());
        
        final SearchConfiguration config = cxt.getSearchResult().getSearchCommand().getSearchConfiguration();

        final int navBarSize = 10;

        if (config.isPaging()) {
            final PagingDisplayHelper pager = new PagingDisplayHelper(
                    cxt.getSearchResult().getHitCount(),
                    config.getResultsToReturn(),   PAGING_SIZE);


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
