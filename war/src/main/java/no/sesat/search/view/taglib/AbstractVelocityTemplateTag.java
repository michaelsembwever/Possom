/* Copyright (2006-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * AbstractImportVelocityTemplateTag.java
 *
 * Created on May 26, 2006, 3:17 PM
 */
package no.sesat.search.view.taglib;


import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.util.OutputConverter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.http.filters.SiteLocatorFilter;
import no.sesat.search.run.RunningQueryImpl;
import no.sesat.search.site.Site;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.TextMessages;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.config.SearchTab.Layout;
import no.sesat.search.view.velocity.VelocityEngineFactory;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;


/** Base class to help with importing velocity templates.
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public abstract class AbstractVelocityTemplateTag extends SimpleTagSupport {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractVelocityTemplateTag.class);
    private static final String ERR_MERGE_FAILURE = "Template merging failed";


    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------


    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------
    
    /** Find (and initialise into the PageContext) the layout this request is to use.
     * 
     * @param datamodel
     * @return
     */
    protected final Layout findLayout(final DataModel datamodel){
        
        Layout layout = null;
        final PageContext cxt = (PageContext) getJspContext();
        if(null != cxt && null != datamodel && null != datamodel.getPage()){
            final SearchTab tab = datamodel.getPage().getCurrentTab();
            final ParametersDataObject params = datamodel.getParameters();
            final StringDataObject layoutDO = null != params ? params.getValue(RunningQueryImpl.PARAM_LAYOUT) : null;
            layout = null != cxt.getAttribute("layout") ? (Layout)cxt.getAttribute("layout") : null != layoutDO && null != tab && null != tab.getLayouts()
                    ? tab.getLayouts().get(layoutDO.getXmlEscaped())
                    : null != tab ? tab.getDefaultLayout() : null;
            cxt.setAttribute("layout", layout);
        }
        return layout;
    }
    
    protected final Site getSiteManually(final PageContext cxt) {
        Site site = null;
        try {
         site = SiteLocatorFilter.getSite(cxt.getRequest());
        }catch (Exception e) {
            LOG.error("Failed to fetch site manually, failing back to '" + Site.DEFAULT.getName() + "': " + e, e);
            site = Site.DEFAULT;
        }
        return site;
    }
        
    /** Imports the specified jsp.
     * 
     * @param include must contain ".jsp" suffix.
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException 
     */
    protected final void importJsp(final String include) throws JspException{
        
        try{
            ((PageContext)getJspContext()).include(include);
            
        }catch(IOException ioe){
            throw new JspException(ioe);
        }catch(ServletException ioe){
            throw new JspException(ioe);
        }
    }  

    /** Imports the specified velocity template.
     * 
     * @param templateName may or may not contain ".vm" extension.
     * @param map key-value pairs to put into the velocity's context.
     * @throws javax.servlet.jsp.JspException 
     */
    protected final void importVelocity(final String templateName, final Map<String,Object> map) throws JspException {

        final String missing = "Missing_" + templateName.replaceAll("/","") + "_Template";

        final PageContext cxt = (PageContext) getJspContext();
        cxt.removeAttribute(missing);
        final JspWriter out = cxt.getOut();

        try {

            final JspFragment f = getJspBody();
            if (f != null){
                f.invoke(out);
            }

            final DataModel datamodel = (DataModel) cxt.findAttribute(DataModel.KEY);

            final Site site = null != datamodel && null != datamodel.getSite()
                    ? datamodel.getSite().getSite()
                    // we haven't gone through the SiteLocatorFilter so get site manually
                    : getSiteManually(cxt);

            assert null != site : "doTag() got null Site";

            final TextMessages text = null != cxt.findAttribute("text")
                    ? (TextMessages)cxt.findAttribute("text")
                    // we haven't gone through the SearchServlet so create TextMessages
                    : TextMessages.valueOf(site);

            assert null != text : "doTag() got null TextMessages";

            final VelocityEngine engine = VelocityEngineFactory.valueOf(site).getEngine();

            final Template template = VelocityEngineFactory.getTemplate(
                    engine, 
                    site, 
                    templateName.replaceAll(".vm$", ""));

            final VelocityContext context = VelocityEngineFactory.newContextInstance(engine);

            // populate context with request and response // TODO remove, since all attributes are copied in
            context.put("request", cxt.getRequest());
            context.put("response", cxt.getResponse());
            // populate context with  search-portal attributes
            for(Map.Entry<String,Object> entry : map.entrySet()){
                context.put(entry.getKey(), entry.getValue());
            }
            context.put("datamodel", datamodel);
            context.put("text", text);
            
            // it's quite long to write $datamodel.site.siteConfiguration.properties so put this in for convenience
            context.put("configuration", null != datamodel && null != datamodel.getSite()
                    ? datamodel.getSite().getSiteConfiguration().getProperties()
                    // we haven't gone through the SiteLocatorFilter so get site manually
                    : SiteConfiguration.instanceOf(site).getProperties());
            
            //context.put("channelCategories", Channel.Category.values());

            // push all parameters into velocity context attributes
            for (@SuppressWarnings("unchecked")
                    Enumeration<String> e = (Enumeration<String>)cxt.getRequest().getAttributeNames()
                    ; e.hasMoreElements();) {

                final String attrName = e.nextElement();
                /* do not overwrite parameters already in the velocity context */
                if (!context.containsKey(attrName)) {
                    context.put(attrName, cxt.getRequest().getAttribute(attrName));
                }
            }

            // populate sitemesh attributes -- will be removed
            final Page siteMeshPage = (Page) cxt.findAttribute(RequestConstants.PAGE);
                if(siteMeshPage != null){
                context.put("page", siteMeshPage);
                context.put("title", OutputConverter.convert(siteMeshPage.getTitle()));
                {
                    final StringWriter buffer = new StringWriter();
                    siteMeshPage.writeBody(OutputConverter.getWriter(buffer));
                    context.put("body", buffer.toString());
                }
                //{ // missing from our version of sitemesh
                //    final StringWriter buffer = new StringWriter();
                //    siteMeshPage.writeHead(OutputConverter.getWriter(buffer));
                //    context.put("head", buffer.toString());
                //}
            }

            // merge it into the JspWriter
            template.merge(context, out);


        } catch (ResourceNotFoundException ex) {
            // often normal usage to 'explore' for templates
            LOG.debug(ex.getMessage());
            cxt.setAttribute(missing, Boolean.TRUE);

        } catch (Exception ex) {
            LOG.error(ERR_MERGE_FAILURE, ex);
            throw new JspException(ex);
        }

    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
