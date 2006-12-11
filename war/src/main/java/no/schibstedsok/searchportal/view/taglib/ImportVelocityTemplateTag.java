// Copyright (2006) Schibsted Søk AS
/*
 * ImportVelocityTemplateTag.java
 *
 * Created on May 26, 2006, 3:17 PM
 */
package no.schibstedsok.searchportal.view.taglib;


import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.util.OutputConverter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.schibstedsok.searchportal.http.filters.SiteLocatorFilter;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.view.i18n.TextMessages;
import no.schibstedsok.searchportal.view.output.VelocityResultHandler;
import no.schibstedsok.searchportal.view.velocity.VelocityEngineFactory;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.MathTool;

/** Imports (and merges) a velocity template from a site-config into the jsp. 
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public final class ImportVelocityTemplateTag extends SimpleTagSupport {

    private static final Logger LOG = Logger.getLogger(ImportVelocityTemplateTag.class);
    private static final String ERR_MERGE_FAILURE = "Template merging failed";
    
    private static final MathTool mathTool = new MathTool();
    /**
     * Initialization of template property.
     */
    private String template;

    /**
     * Initialization of command property.
     */
    private String command;

    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    public void doTag() throws JspException {

        final String missing = "Missing_" + this.template.replaceAll("/","") + "_Template";

        final PageContext cxt = (PageContext) getJspContext();
        cxt.removeAttribute(missing);
        final JspWriter out = cxt.getOut();

        try {

            final JspFragment f = getJspBody();
            if (f != null){
                f.invoke(out);
            }
            
            final Site site = null != cxt.findAttribute(Site.NAME_KEY)
                    ? (Site)cxt.findAttribute(Site.NAME_KEY)
                    // we haven't gone through the SiteLocatorFilter so get site manually
                    : SiteLocatorFilter.getSite(cxt.getRequest());
            
            assert null != site : "doTag() got null Site";
            
            final TextMessages text = null != cxt.findAttribute("text")
                    ? (TextMessages)cxt.findAttribute("text")
                    // we haven't gone through the SearchServlet so create TextMessages
                    : TextMessages.valueOf(site);
            
            assert null != text : "doTag() got null TextMessages";

            final VelocityEngine engine = VelocityEngineFactory.valueOf(site).getEngine();
            final Template template = VelocityResultHandler.getTemplate(engine, site, this.template);
            
            if (template != null){

                final VelocityContext context = VelocityResultHandler.newContextInstance(engine);

                // populate context with request and response // TODO remove, since all attributes are copied in
                context.put("request", cxt.getRequest());
                context.put("response", cxt.getResponse());
                // populate context with  search-portal attributes
                context.put("commandName", command != null ? command : this.template);
                context.put("base", ((HttpServletRequest)cxt.getRequest()).getContextPath());
                context.put("contextPath", ((HttpServletRequest)cxt.getRequest()).getContextPath());
                context.put("text", text);
                context.put("configuration", SiteConfiguration.valueOf(site).getProperties());
                context.put("math", mathTool);

                // push all parameters into velocity context attributes
                for (Enumeration<String> e = (Enumeration<String>)cxt.getRequest().getAttributeNames(); e.hasMoreElements();) {
                    final String attrName = e.nextElement();
                    /* do not overwrite parameters already in the velocity context */
                    if (!context.containsKey(attrName)) {
                        context.put(attrName, cxt.getRequest().getAttribute(attrName));
                    }
                }

                // populate modifiers
                final List<Modifier> sources = (List<Modifier>) cxt.findAttribute("sources");
                if(sources != null){
                    for (Modifier mod : sources) {
                        if (mod.getName().equals("sesam_hits")) {
                            context.put("sesam_hits", text.getMessage("numberFormat", mod.getCount()));
                        }
                    }
                }

                // populate sitemesh attributes
                final Page siteMeshPage = (Page) cxt.findAttribute(RequestConstants.PAGE);
                    if(siteMeshPage != null){
                    context.put("page", siteMeshPage);
                    context.put("title", OutputConverter.convert(siteMeshPage.getTitle()));
                    {
                        final StringWriter buffer = new StringWriter();
                        siteMeshPage.writeBody(OutputConverter.getWriter(buffer));
                        context.put("body", buffer.toString());
                    }
                    //{ // missing frm our version of sitemesh
                    //    final StringWriter buffer = new StringWriter();
                    //    siteMeshPage.writeHead(OutputConverter.getWriter(buffer));
                    //    context.put("head", buffer.toString());
                    //}
                }

                // merge it into the JspWriter
                template.merge(context, out);

            }else{
                cxt.setAttribute(missing, Boolean.TRUE);
            }

        } catch (Exception ex) {
            LOG.error(ERR_MERGE_FAILURE, ex);
            throw new JspException(ex);
        }

    }

    /**
     * Setter for the template attribute.
     */
    public void setTemplate(final String value) {
        this.template = value;
    }

    /**
     * Setter for the command attribute.
     */
    public void setCommand(final String value) {
        this.command = value;
    }
}
