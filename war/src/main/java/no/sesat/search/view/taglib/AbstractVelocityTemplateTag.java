/* Copyright (2006-2008) Schibsted ASA
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspContext;
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
 *
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

    /**
     * Find the layout that belongs to this model.
     *
     * @param datamodel
     * @return Layout
     */
    public static Layout findLayout(final DataModel datamodel){

        Layout layout = null;
        if(null != datamodel && null != datamodel.getPage()){
            final SearchTab tab = datamodel.getPage().getCurrentTab();
            final ParametersDataObject params = datamodel.getParameters();

            StringDataObject layoutDO = null;
            if (params != null) {
                layoutDO = params.getValue(RunningQueryImpl.PARAM_LAYOUT);
                if (layoutDO == null) {
                    layoutDO = params.getValue(RunningQueryImpl.PARAM_LAYOUT_OLD);
                }
            }

            layout = null != layoutDO && null != tab && null != tab.getLayouts()
                    ? tab.getLayouts().get(layoutDO.getXmlEscaped())
                    : null != tab ? tab.getDefaultLayout() : null;
        }
        return layout;
    }

    /**
     * Find the layout from the context. If not found in context we look for it
     * in the datamodel and set it in the context.
     *
     * @param cxt
     * @return Layout
     */
    protected Layout findLayout(final JspContext cxt) {
        Layout layout = null;
        if (cxt != null) {
            layout = (Layout)cxt.getAttribute("layout");
            if (layout == null) {
                layout = findLayout((DataModel) cxt.findAttribute(DataModel.KEY));
                cxt.setAttribute("layout", layout);
            }
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

   /** Forward to the specified jsp.
    *
    * @param include must contain ".jsp" suffix.
    * @throws java.io.IOException
    * @throws javax.servlet.ServletException
    */
   protected final void forwardJsp(final String include) throws JspException{

       try{
           ((PageContext)getJspContext()).forward(include);
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

            final VelocityContext context = VelocityEngineFactory.newContextInstance();

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
                // do not overwrite parameters already in the velocity context
                if (!context.containsKey(attrName)) {
                    context.put(attrName, cxt.getRequest().getAttribute(attrName));
                }
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
