/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 * ResourceServlet.java
 *
 * Created on 19 January 2006, 13:51
 */

package no.schibstedsok.commons.resourcefeed;


import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Iterator;

/** Resource Provider.
 * Serves configuration files (properties, xml), css, gifs, jpgs, javascript,
 * classes, jar files and velocity templates for search-portal.
 * Css, images, and javascript require direct access from client.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class ResourceServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ResourceServlet.class);

    private static final String REMOTE_ADDRESS_KEY = "REMOTE_ADDR";
    private static final String ERR_RESTRICTED_AREA = "<strong>Restricted Area!</strong>";
    private static final String ERR_TRIED_TO_ACCESS = " tried to access Resource servlet!";
    private static final String ERR_NOT_FOUND = "Failed to find resource ";
    private static final String ERR_TRIED_TO_CROSS_REFERENCE = " tried to cross-reference resource!";

    private static final String DEBUG_DEFAULT_MODIFCATION_TIMESTAMP = "Default modified timestamp set to ";
    private static final String DEBUG_CLIENT_IP = "Client ipaddress ";

    private static final Map<String,String> CONTENT_TYPES = new HashMap<String,String>();
    private static final Map<String,String> CONTENT_PATHS = new HashMap<String,String>();
    private static final Set<String> RESTRICTED = new HashSet<String>();

    private long defaultLastModified = 0;
    private String[] ipaddressesAllowed = new String[]{};

    private ServletConfig servletConfig;

    static {
        // The different extension to content type mappings
        // XXX is there an opensource library to do this?
        CONTENT_TYPES.put("properties", "text/plain");
        CONTENT_TYPES.put("xml", "text/xml");
        CONTENT_TYPES.put("css", "text/css");
        CONTENT_TYPES.put("js", "text/javascript");
        CONTENT_TYPES.put("jpg", "image/jpeg");
        CONTENT_TYPES.put("gif", "image/gif");
        CONTENT_TYPES.put("png", "image/png");
        CONTENT_TYPES.put("ico", "image/x-icon");
        CONTENT_TYPES.put("vm", "text/plain");
        CONTENT_TYPES.put("html", "text/plain");
        CONTENT_TYPES.put("class", "application/java");
        CONTENT_TYPES.put("jar", "application/java-archive");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletInfo() {

        return "Servlet responsible for serving resources. Goes in hand with search-portal/site-spi";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ServletConfig config) {

        this.servletConfig = config;

        defaultLastModified = System.currentTimeMillis();
        LOG.info(DEBUG_DEFAULT_MODIFCATION_TIMESTAMP + defaultLastModified);

        final String allowed = config.getInitParameter("ipaddresses.allowed");
        LOG.info("allowing ipaddresses " + allowed);
        if (null != allowed && allowed.length() >0) {
            ipaddressesAllowed = allowed.split(",");
        }

        final String restricted = config.getInitParameter("resources.restricted");
        LOG.info("restricted resources " + restricted);
        if (null != restricted && restricted.length()>0) {
            RESTRICTED.addAll(Arrays.asList(restricted.split(",")));
        }

        final String paths = config.getInitParameter("content.paths");
        LOG.info("content path mappings " + paths);
        if (null != paths && paths.length()>0) {
            final String[] pathArr = paths.split(",");
            for (String path : pathArr) {
                final String[] pair = path.split("=");
                CONTENT_PATHS.put(pair[0], pair[1]);
            }
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * This servlet ignores URL parameters and POST content, as all the information is in the path,
     * so it really doesn't matter if it is a GET or POST.
     *
     * Checks:
     *  - resource exists,
     *  - correct path is being used,
     *  - configuration/template resources are only accessed by schibsted machines,
     *
     * The resource is served to the ServletOutputStream byte by byte from
     *  getClass().getResourceAsStream(..)
     *
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if ServletException occurs
     * @throws java.io.IOException if IOException occurs
     */
    protected void processRequest(
            final HttpServletRequest request,
            final HttpServletResponse response)
                throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // correct encoding

        // Get resource name. Also strip the version number out of the resource
        final String configName = request.getPathInfo().replaceAll("/(\\d)+/","/");

        if (configName != null && configName.trim().length() > 0) {


            final String extension = configName.substring(configName.lastIndexOf('.') + 1).toLowerCase();
            final String ipAddr = null != request.getAttribute(REMOTE_ADDRESS_KEY)
                ? (String) request.getAttribute(REMOTE_ADDRESS_KEY)
                : request.getRemoteAddr();

            // Content-Type
            response.setContentType(CONTENT_TYPES.get(extension) + ";charset=UTF-8");

            // Path check. Resource can only be loaded through correct path.
            final String directory = request.getServletPath();
            if (null != CONTENT_PATHS.get(extension) && directory.indexOf(CONTENT_PATHS.get(extension)) >= 0) {

                // ok, check configuration resources are private.
                LOG.trace(DEBUG_CLIENT_IP + ipAddr);

                if (RESTRICTED.contains(extension) && !isIpAllowed(ipAddr)) {

                    response.setContentType("text/html;charset=UTF-8");
                    response.getOutputStream().print(ERR_RESTRICTED_AREA);
                    LOG.warn(ipAddr + ERR_TRIED_TO_ACCESS);

                }  else  {
                    serveResource(configName, request, response);
                }
            }  else  {
                // not allowed to cross-reference resources.
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                LOG.warn(ipAddr + ERR_TRIED_TO_CROSS_REFERENCE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(
            final HttpServletRequest request,
            final HttpServletResponse response)
                throws ServletException, IOException {

        processRequest(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(
            final HttpServletRequest request,
            final HttpServletResponse response)
                throws ServletException, IOException {

        processRequest(request, response);
    }

    /** Assigned to the time when the servlet is initialised via the init(ServletConfig) method.
     * Any redeployment of the skin results in an update in the last-modified response header.
     * Editing the files "in-place" on disk will not have any effect on the last-modified header.
     *
     * @param req incoming HttpServletRequest request
     * @return last-modified header (in milliseconds)
     **/
    @Override
    protected long getLastModified(final HttpServletRequest req) {
        return defaultLastModified;
    }

    private void serveResource(
            final String configName,
            final HttpServletRequest request,
            final HttpServletResponse response)
                throws ServletException, IOException {

        InputStream is = null;

        try  {
            is = configName.endsWith(".jar")
                    ? getJarStream(configName) : ResourceServlet.class.getResourceAsStream(configName);

            if (is != null) {

                // Write response headers before response data according to javadoc for HttpServlet.html#doGet(..)

                // Allow this URL to be cached indefinitely.
                //  Each jvm restart alters the number that appears in the URL being enough to ensure
                //  nothing is cached across deployment versions.
                response.setHeader("Cache-Control", "Public");
                response.setDateHeader("Expires", Long.MAX_VALUE);

                // Avoid writing out the response body if it's a HEAD request or a GET that the browser has cache for
                boolean writeBody = !"HEAD".equals(request.getMethod());
                writeBody &= request.getDateHeader("If-Modified-Since") <= defaultLastModified;

                if (writeBody) {

                    // Output the resource byte for byte
                    final OutputStream os = response.getOutputStream();
                    for (int b = is.read(); b >= 0; b = is.read()) {
                         os.write(b);
                    }

                    // commit response now
                    os.flush();
                }

            }  else  {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                LOG.info(ERR_NOT_FOUND + request.getPathInfo());
            }

        }  finally  {

            if (is != null) {
                is.close();
            }
        }
    }

    private InputStream getJarStream(final String resource) {
        final Set paths = servletConfig.getServletContext().getResourcePaths("/WEB-INF/lib");

        final String baseName = resource.replace(".jar", "").replace("/", "");

        try {
            for (final Iterator iterator = paths.iterator(); iterator.hasNext();) {

                final String path = (String) iterator.next();

                // Remove path, site name and version suffix.
                final String jarName = path.substring(path.lastIndexOf('/') + 1).replaceAll("-(\\d+\\.?)+(-SNAPSHOT)?.jar$", "").replaceAll("^([\\p{Alnum}]+\\.?)+-", "");

                if (jarName.equals(baseName)) {
                    final URL url = servletConfig.getServletContext().getResource(path);
                    return url.openConnection().getInputStream();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Returns wether we allow the ipaddress or not.
     * @param ipAddr the ipaddress to check.
     *
     * @return returns true if the ip address is trusted.
     */
   private boolean isIpAllowed(final String ipAddr) {

	 boolean allowed =
                 ipAddr.startsWith("127.") || ipAddr.startsWith("10.") || ipAddr.startsWith("0:0:0:0:0:0:0:1%0");

         for(String s : ipaddressesAllowed){
             allowed |= ipAddr.startsWith(s);
         }
         return allowed;

    }

}
