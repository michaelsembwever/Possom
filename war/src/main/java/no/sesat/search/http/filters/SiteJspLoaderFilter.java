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
 */
package no.sesat.search.http.filters;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import no.sesat.search.site.config.ResourceLoadException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.config.UrlResourceLoader;
import org.apache.log4j.Logger;


/**
 * Downloads JSP files from skins into sesat to be compiled and used locally.
 * This makes it look like jsps from the other skin web applications actually are bundled into sesat. <br/><br/>
 *
 * Implementation issue: <a href="https://jira.sesam.no/jira/browse/SEARCH-4290">Design and code with JSPs in skins</a>
 *
 * <br/><br/>
 * 
 * <b>Inclusion of jsps</b> may occurr with &lt;jsp:include page="..."/> 
 *      or some other requestDispatcher.include(..) approach.
 *      &lt;%@ include file=".."/%> will not work.
 * 
 * 
 * <br/><br/>
 *
 * <b>To enable JSP files</b> in a particular skin to be downloaded into sesat the following configuration is required:
 *  <ul>
 * <li>in the skin's web.xml add "jsp" to the resources.restricted init-param for ResourceServlet,</li>
 * <li>in the skin's web.xml add "jsp=jsp" to the content.paths init-param for ResourceServlet,</li>
 * <li>in the skin's web.xml add the servlet-mapping:
 * <pre>
    &lt;servlet-mapping>
        &lt;servlet-name>resource servlet&lt;/servlet-name>
        &lt;url-pattern>*.jsp&lt;/url-pattern>
    &lt;/servlet-mapping>
 * </pre> so to avoid the skin's JspServlet and to serve the jsp files are resources back to sesat,</li>
 *  </ul>
 * This have already been done in the base skin sesat-kernel/generic.sesam and can be used as an example.<br/><br/>
 *
 * <b>Tomcat, or the container used, must not use unpackWARs="false", or any non-file based deployment implementation,
 * as this class must be able to write files into the deployed webapps directory.</b> <br/>
 * Such files are written using a  FileChannel obtained like
 * <pre>new RandomFileAccess(new File(root + "requested-jsp-name"),"rw").getChannel()</pre>
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version $Id$
 */
public final class SiteJspLoaderFilter implements Filter {


    private static final Logger LOG = Logger.getLogger(SiteJspLoaderFilter.class);

    private FilterConfig config;
    private String root;

    public void init(final FilterConfig filterConfig) throws ServletException {

        config = filterConfig;
        root = config.getServletContext().getRealPath("/");
    }

    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        if( request instanceof HttpServletRequest){

            final String jsp = getRequestedJsp((HttpServletRequest)request);
            LOG.debug("jsp: " + jsp + "; resource: " + config.getServletContext().getResource(jsp));

            downloadJsp((HttpServletRequest)request, jsp);

        }

        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    // copied from JspServlet.serve(..)
    private String getRequestedJsp(
            final HttpServletRequest request){

        String jspUri = null;

        String jspFile = (String) request.getAttribute(JSP_FILE);
        if (jspFile != null) {
            // JSP is specified via <jsp-file> in <servlet> declaration
            jspUri = jspFile;
        } else {
            /*
             * Check to see if the requested JSP has been the target of a
             * RequestDispatcher.include()
             */
            jspUri = (String) request.getAttribute(INC_SERVLET_PATH);
            if (jspUri != null) {
                /*
		 * Requested JSP has been target of
                 * RequestDispatcher.include(). Its path is assembled from the
                 * relevant javax.servlet.include.* request attributes
                 */
                String pathInfo = (String) request.getAttribute("javax.servlet.include.path_info");
                if (pathInfo != null) {
                    jspUri += pathInfo;
                }
            } else {
                /*
                 * Requested JSP has not been the target of a
                 * RequestDispatcher.include(). Reconstruct its path from the
                 * request's getServletPath() and getPathInfo()
                 */
                jspUri = request.getServletPath();
                String pathInfo = request.getPathInfo();
                if (pathInfo != null) {
                    jspUri += pathInfo;
                }
            }
        }
        return jspUri;
    }

    private void downloadJsp(
            final HttpServletRequest request,
            final String jsp) throws MalformedURLException{

        byte[] golden = new byte[0];

        // search skins for the jsp and write it out to "golden"
        for(Site site = (Site) request.getAttribute(Site.NAME_KEY); 0 == golden.length; site = site.getParent()){

            if(null == site){
                if(null == config.getServletContext().getResource(jsp)){
                    throw new ResourceLoadException("Unable to find " + jsp + " in any skin");
                }
                break;
            }

            final Site finalSite = site;
            final BytecodeLoader bcLoader = UrlResourceLoader.newBytecodeLoader(
                    new SiteContext(){
                        public Site getSite() {
                            return finalSite;
                        }
                    },
                    jsp,
                    null
            );
            bcLoader.abut();
            golden = bcLoader.getBytecode();
        }

        // if golden now contains data save it to a local (ie local web application) file
        if(0 < golden.length){
            try {
                final File file = new File(root + jsp);

                // create the directory structure
                file.getParentFile().mkdirs();

                // check existing file
                boolean needsUpdating = true;
                final boolean fileExisted = file.exists();
                if(!fileExisted){
                    file.createNewFile();
                }
                final RandomAccessFile fileAccess = new RandomAccessFile(file, "rw");
                final FileChannel channel = fileAccess.getChannel();

                try{
                    // channel.lock() only synchronises file access between programs, but not between threads inside 
                    //  the current JVM. The latter results in the OverlappingFileLockException.
                    //  At least this is my current understanding of java.nio.channels
                    //   It may be that no synchronisation or locking is required at all. A beer to whom answers :-)
                    // So we must provide synchronisation between our own threads,
                    //  synchronisation against the file's path (using the JVM's String.intern() functionality)
                    //  should work. (I can't imagine this string be used for any other synchronisation purposes).
                    synchronized(file.toString().intern()){
                        channel.lock();

                        if(fileExisted){

                            final byte[] bytes = new byte[(int)channel.size()];
                            final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                            int reads; do{ reads = channel.read(byteBuffer); }while(0 < reads);

                            needsUpdating = !Arrays.equals(golden, bytes);
                        }

                        if(needsUpdating){
                            // download file from skin
                            channel.write(ByteBuffer.wrap(golden), 0);
                            channel.force(true);
                            file.deleteOnExit();

                        }
                    }
                }finally{
                    channel.close();
                    LOG.debug("resource created as " + config.getServletContext().getResource(jsp));

                }

            }catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    //// Imported from org.catalina.jasper.Constants

    /**
     * Request attribute for <code>&lt;jsp-file&gt;</code> element of a
     * servlet definition.  If present on a request, this overrides the
     * value returned by <code>request.getServletPath()</code> to select
     * the JSP page to be executed.
     */
    public static final String JSP_FILE =
        System.getProperty("org.apache.jasper.Constants.JSP_FILE", "org.apache.catalina.jsp_file");

    /**
     * Servlet context and request attributes that the JSP engine
     * uses.
     */
    public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";

}
