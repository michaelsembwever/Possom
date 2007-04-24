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
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/** Utility class to fetch URLs and return them as either BufferedReaders or XML documents.
 * Keeps statistics on connection times and failures.
 * TODO Provide Quality of Service through ramp up/down and throttling functionality.
 * XXX redesign into multiple classes with less static methods.
 *
 * Supports protocols http, https, ftp, and file.
 * If no protocol is specified in the host it defaults to http.
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

    private final String id;
    private final String host, hostHeader;
    private final int port;
    private volatile URLConnection urlConn;

    private HTTPClient(final String host, final int port, final String hostHeader) {

        this.host= host;
        this.port = port;
        this.hostHeader = hostHeader;

        String id = host + ':' + port;
        if(id.contains("://")){
            id = id.substring(id.indexOf("://") + 3);
        }
        this.id = id;
        
    }

    /**
     *
     * @param host
     * @param port
     * @return
     */
    public static HTTPClient instance(final String host, final int port) {

        return new HTTPClient(host, port, host);
    }

    /**
     *
     * @param host
     * @param port
     * @param hostHeader
     * @return
     */
    public static HTTPClient instance(final String host, final int port, final String hostHeader) {

        return new HTTPClient(host, port, hostHeader);
    }

    private URL getURL(final String path) throws MalformedURLException{

        if( port == 0 ){
            throw new MalformedURLException("Null port");
        }

        final boolean hasProtocol = host.startsWith("http://") || host.startsWith("https://")
                || host.startsWith("ftp://") || host.startsWith("file://");
                //host.matches("^(http|https|ftp|file)://");

        final URL url = new URL(
                (hasProtocol ? "" : "http://")
                + host + ':' + port
                + path);

        LOG.trace(DEBUG_USING_URL + url);

        return url;
    }

    /**
     *
     * @param path
     * @return
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public Document getXmlDocument(final String path) throws IOException, SAXException {

        final int priority = Thread.currentThread().getPriority();
        loadUrlConnection(path);

        if(!hostHeader.equals(host)){

            LOG.debug(DEBUG_USING_HOSTHEADER + hostHeader);
            urlConn.addRequestProperty("host", hostHeader);
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();

            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            final long start = System.nanoTime();

            final Document result = builder.parse(urlConn.getInputStream());

            Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);

            return result;

        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());

        } catch (IOException e) {
            throw interceptIOException(e);

        }finally{
            Thread.currentThread().setPriority(priority);
            
            if(null != urlConn && null != urlConn.getInputStream()){
                urlConn.getInputStream().close();
            }
            if(null != urlConn){
                // definitely done with connection now
                urlConn = null;
            }
        }
    }

    /**
     *
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public BufferedInputStream getBufferedStream(final String path) throws IOException {

        final int priority = Thread.currentThread().getPriority();
        loadUrlConnection(path);

        try{
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            final long start = System.nanoTime();

            final BufferedInputStream result = new BufferedInputStream(urlConn.getInputStream());

            Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);

            return result;

        } catch (IOException e) {
            throw interceptIOException(e);

        }finally{
            Thread.currentThread().setPriority(priority);
        }
    }

    /**
     *
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public BufferedReader getBufferedReader(final String path) throws IOException {

        final int priority = Thread.currentThread().getPriority();
        loadUrlConnection(path);

        try{
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            final long start = System.nanoTime();

            final BufferedReader result = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);

            return result;

        } catch (IOException e) {
            throw interceptIOException(e);

        }finally{
            Thread.currentThread().setPriority(priority);
        }
    }

    /**
     *
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public long getLastModified(final String path) throws IOException {

        try{
            return loadUrlConnection(path).getLastModified();
            
        } catch (IOException e) {
            throw interceptIOException(e);

        }finally{
            urlConn = null;
        }
    }

    /**
     *
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public boolean exists(final String path) throws IOException {

        boolean success = false;
        loadUrlConnection(path);

        if(urlConn instanceof HttpURLConnection){
            try{
                
                final HttpURLConnection con = (HttpURLConnection)urlConn;
                con.setInstanceFollowRedirects(false);
                con.setRequestMethod("HEAD");
                con.addRequestProperty("host", hostHeader);
                con.setConnectTimeout(1000);
                con.setReadTimeout(1000);
                success = HttpURLConnection.HTTP_OK == con.getResponseCode();
                
            } catch (IOException e) {
                throw interceptIOException(e);

            }finally{
                urlConn = null;
            }
            
        }else{
            
            final File file = new File(path);
            success = file.exists();
        }
        return success;
    }
    
    /**
     * 
     * @param ioe 
     * @return 
     */
    public IOException interceptIOException(final IOException ioe){
        
        final IOException e = interceptIOException(id, urlConn, ioe);
        
        // definitely done with connection now
        urlConn = null;
        
        return e;
    }
    
    /**
     * 
     * @param conn 
     * @param ioe 
     * @return 
     */
    public static IOException interceptIOException(
            final URLConnection conn, 
            final IOException ioe){
        
        final String id = conn.getURL().getHost() + ':' + conn.getURL().getPort();
        
        return interceptIOException(id, conn, ioe);
    }
             
    /**
     * 
     * @param id 
     * @param urlConn 
     * @param ioe 
     * @return 
     */
    public static IOException interceptIOException(
            final String id, 
            final URLConnection urlConn, 
            final IOException ioe){
        
        if( ioe instanceof SocketTimeoutException){
            Statistic.getStatistic(id).addReadTimeout();
            
        }else if( ioe instanceof ConnectException){
            Statistic.getStatistic(id).addConnectTimeout();
            
        }else{
            Statistic.getStatistic(id).addFailure();
        }

        // Clean out the error stream. See
        if(urlConn instanceof HttpURLConnection){
            cleanErrorStream((HttpURLConnection)urlConn);
        }
        
        
        return ioe;
    }
    
    
    /**
     * 
     * @param conn 
     * @param time 
     */
    public static void addConnectionStatistic(final URLConnection conn, final long time){ 
        
        final String id = conn.getURL().getHost() + ':' + conn.getURL().getPort();
        Statistic.getStatistic(id).addInvocation(time);
    }
    
    private URLConnection loadUrlConnection(final String path) throws IOException {

        if(null == urlConn){
            urlConn = getURL(path).openConnection();

            if( !hostHeader.equals(host) ){
                LOG.trace(DEBUG_USING_HOSTHEADER + hostHeader);
                urlConn.addRequestProperty("host", hostHeader);
            }
        }
        return urlConn;
    }

    private static void cleanErrorStream(final HttpURLConnection con){

        if(null != con.getErrorStream()){
            final BufferedReader errReader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            final StringBuilder err = new StringBuilder();
            try{
                for(String line = errReader.readLine(); null != line; line = errReader.readLine()){
                    err.append(line);
                }
            }catch(IOException ioe){
                LOG.warn(ioe.getMessage(), ioe);
            }
            LOG.info(err.toString());
        }
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



    private static class Statistic implements Comparable<Statistic>{
        

        private static final Map<String,Statistic> STATISTICS = new ConcurrentHashMap<String,Statistic>();
    
        private static final Logger STATISTICS_LOG = Logger.getLogger(Statistic.class);        
            
        private final String id;
        private long totalTime = 0;
        private long invocations = 0;
        private volatile long connectTimeouts = 0;
        private volatile long readTimeouts = 0;
        private volatile long failures = 0;
        private static volatile long lastPrint = System.currentTimeMillis() / 60000;
        
        static Statistic getStatistic(final String id){
            
            if(null==STATISTICS.get(id)){
                STATISTICS.put(id, new Statistic(id));
            }
            return STATISTICS.get(id);
        }

        private Statistic(final String id){
            this.id = id;
        }

        synchronized void addInvocation(final long time){

            totalTime += (time / 1000000);
            ++invocations;
            
            if(STATISTICS_LOG.isDebugEnabled() && System.currentTimeMillis() / 60000 != lastPrint){ 
                
                printStatistics(); 
                lastPrint = System.currentTimeMillis() / 60000;
            }
        }
        
        void addFailure(){
            ++failures;
        }
        
        void addConnectTimeout(){
            ++connectTimeouts;
        }
        
        void addReadTimeout(){
            ++readTimeouts;
        }
        
        private long getAverageInvocationTime(){
            return 0 < invocations ? (totalTime * (long)1000 / invocations) : 0;
        }

        @Override
        public String toString() {
                        
            return ": " +  new DecimalFormat("000,000,000").format(invocations)
                    + " : " + new DecimalFormat("000,000,000").format(totalTime)
                    + "ms : " + new DecimalFormat("0,000,000").format(getAverageInvocationTime())
                    + "µs :   " + new DecimalFormat("00,000").format(failures)
                    + " :         " + new DecimalFormat("00,000").format(connectTimeouts)
                    + " : " + new DecimalFormat("00,000").format(readTimeouts)
                    + " <-- " + id;
        }
        
        public int compareTo(Statistic o) {
            return (int) (o.getAverageInvocationTime() - getAverageInvocationTime());
        }
       

        private static void printStatistics(){
        
            final List<Statistic> list = new ArrayList<Statistic>(STATISTICS.values());
            Collections.sort(list);

            final StringBuilder msg = new StringBuilder();
            msg.append("\n------ Printing HTTPClient statistics ------\n"
                    + ": invocations : total time    : average     "
                    + ": failures : connect errors : read timeouts <- client\n");
            
            for(Statistic stat : list){
                msg.append(stat.toString() + '\n');
            }
            msg.append("------ ------------------------------ ------");
            STATISTICS_LOG.debug(msg.toString());
        }        
    }
}
