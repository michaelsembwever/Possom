// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import org.apache.log4j.Logger;

/** Utility class to fetch URLs and return them as either BufferedReaders or XML documents.
 * Original implementation used Commons HttpClient but keepalive was disabled, due to a FAST Query-Matching bug,
 *  which was its original benefit.
 * The current implementation just using URL and URLConnection directly synchronously.
 * 
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@sesam.no">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class HTTPClient {
    
    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(HTTPClient.class);
    private static final String DEBUG_USING_URL = "Using url ";
    private static final String DEBUG_USING_HOSTHEADER = "Using host header: ";
    
    private final String host, hostHeader;
    private final int port;
    
    private HTTPClient(final String host, final int port, final String hostHeader) {
        
        this.host= host;
        this.port = port;
        this.hostHeader = hostHeader;
    }

    public static HTTPClient instance(final String id, final String host, final int port) {
        
        return new HTTPClient(host, port, host);
    }
    
    public static HTTPClient instance(final String id, final String host, final int port, final String hostHeader) {
        
        return new HTTPClient(host, port, hostHeader);
    }
       
    private URL getURL(final String path) throws MalformedURLException{
        
        if( port == 0 ){
            throw new MalformedURLException("Null port");
        }
        
        final URL url = new URL(
                (host.startsWith("http://") ? "" : "http://")
                + host + ':' + port 
                + path);
        
        LOG.debug(DEBUG_USING_URL + url);
        
        return url;
    }

    public Document getXmlDocument(final String id, final String path) throws IOException, SAXException {
            
            
        final URL url = getURL(path);
        final URLConnection urlConn = url.openConnection();

        if( !hostHeader.equals(host) ){
            LOG.debug(DEBUG_USING_HOSTHEADER + hostHeader);
            urlConn.addRequestProperty("host", hostHeader);
        }


        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
            return builder.parse(urlConn.getInputStream());

        } catch (ParserConfigurationException e) {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
            throw new IOException(e.getMessage());
        }
    }
    
    public BufferedInputStream getBufferedStream(final String id, final String path) throws IOException {
            
        return new BufferedInputStream(getStream(id, path));
    }
    
    public BufferedReader getBufferedReader(final String id, final String path) throws IOException {
          
        return new BufferedReader(new InputStreamReader(getStream(id, path)));
    }
    
    public long getLastModified(final String id, final String path) throws IOException {

        return getUrlConnection(id, path).getLastModified();
    }
    
    public boolean exists(final String id, final String path) throws IOException {

        boolean success = false;
        final URLConnection conn = getUrlConnection(id, path);

        if(conn instanceof HttpURLConnection){
            final HttpURLConnection con = (HttpURLConnection)conn;
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("HEAD");
            con.addRequestProperty("host", hostHeader);
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            success = HttpURLConnection.HTTP_OK == con.getResponseCode();      
        }else{
            final File file = new File(path);
            success = file.exists();
        }
        return success;
    }
    
    private InputStream getStream(final String id, final String path) throws IOException {

        return getUrlConnection(id, path).getInputStream();
    }
    
    private URLConnection getUrlConnection(final String id, final String path) throws IOException {
            
        final URLConnection urlConn = getURL(path).openConnection();

        if( !hostHeader.equals(host) ){
            LOG.debug(DEBUG_USING_HOSTHEADER + hostHeader);
            urlConn.addRequestProperty("host", hostHeader);
        }

        return urlConn;
    }
    
    
    // --HTTPClient implementation to allow keepalive or pipelining.
    // --  see revision 3596 for original implementation
    
//    private static final String DEBUG_ADDING_CONF = "Adding configuration ";
    
//    private static final Map<String,HostConfiguration> hostConfigurations = new HashMap<String,HostConfiguration>();
//
//    private static final HttpConnectionManager cMgr = new ConnectionManagerWithoutKeepAlive();
//    private HttpClient commonsHttpClient;

//    private static final HTTPClient client = new HTTPClient();
    
    
    
//    private HTTPClient() {
//        final HttpConnectionManagerParams params = new HttpConnectionManagerParams();
//        params.setStaleCheckingEnabled(true);
//        params.setMaxTotalConnections(Integer.MAX_VALUE                                                                                                                                                                                                                                                                                                                                                         );
//        if(Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)){
//            params.setSoTimeout(3000);
//        }
//        cMgr.setParams(params);
//        commonsHttpClient = new HttpClient(cMgr);
//    }
    
    
//    public static HTTPClient instance(final String id, final String host, final int port) {
//        
//        if (!hostConfigurations.containsKey(id)) {
//            addHostConfiguration(id, host, port);
//        }
//    }
    
//    private HttpMethod executeGet(final String id, final String path) throws IOException {
//        
//        final HostConfiguration conf = (HostConfiguration) hostConfigurations.get(id);
//        final HttpMethod method = new GetMethod(path);
//        commonsHttpClient.executeMethod(conf, method);
//        return method;
//    } 
    
    
//    private void release(final HttpMethod method) {
//        method.releaseConnection();
//    }
//
//    private synchronized static void addHostConfiguration(final String id, final String host, final int port) {
//        
//        if (! hostConfigurations.containsKey(id)) {
//            
//            final HostConfiguration conf = new HostConfiguration();
//            LOG.debug(DEBUG_ADDING_CONF + host + ":" + port);
//            conf.setHost(host, port, "http");
//            cMgr.getParams().setMaxConnectionsPerHost(conf, 1);
//            hostConfigurations.put(id, conf);
//        }
//    }
    
}
