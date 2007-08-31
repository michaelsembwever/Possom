/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.http;

import java.util.jar.JarFile;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.JarURLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import sun.net.www.protocol.jar.URLJarFile;
import sun.net.www.protocol.jar.URLJarFileCallBack;

/**
 * Utility class to fetch URLs and return them as either BufferedReaders or XML documents.
 * Keeps statistics on connection times and failures.
 * XXX redesign into multiple classes with less static methods.
 * <p/>
 * Supports protocols http, https, ftp, jar, and file.
 * If no protocol is specified in the host it defaults to http.
 * Provides support for URL Jars loaded with request properties as Sun's JVM does not.
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6270774
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@sesam.no">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class HTTPClient {

    // Constants -----------------------------------------------------

    private static final int CONNECT_TIMEOUT = 1000; // milliseconds
    private static final int READ_TIMEOUT = 1000; // millisceonds

    private static final Logger LOG = Logger.getLogger(HTTPClient.class);
    private static final String DEBUG_USING_URL = "Using url {0} and Host-header {1} ";

    // Attributes ----------------------------------------------------
    
    private final String id;
    private URLConnection urlConn;
    private final URL u;
    private final PhysicalHostStreamHandler handler;

    // Static --------------------------------------------------------

    /**
     * Returns client for specified host and port for HTTP protocol.
     *
     * @param host The host to use. If no protocol is given then http is assumed.
     * @param port The port to use.
     *
     * @return a client.
     */
    public static HTTPClient instance(final String host, final int port) {
        
        assert !host.contains("://") : "Not allowed to specify protocol, use another instance method.";
        
        return instance(host, port, host);
    }

    /**
     * Returns client for specified host, port and physical host (if the host is virtual). 
     * Useful if you need to use a virtual host different
     * from the physical host.
     * Defaults to the http protocol if the host argument doesn't specify it.
     *
     * @param host the physical host to use.
     * @param port the port to use.
     * @param hostHeader virtual host to use.
     *
     * @return a client.
     */
    public static HTTPClient instance(final String host, final int port, final String physicalHost) {
        
        try {
            return new HTTPClient(new URL(ensureProtocol(host) + ':' + port), physicalHost);
            
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns client instance for the specified URL. The URL can either be complete or just contain the host.
     * 
     * Note that only the host and port and used since the url must be supplied again against the HTTPClient instance.
     * 
     * The path can be supplied later when using the querying methods like
     * {@link HTTPClient#getBufferedStream(String path)}.
     *
     * @param url The URL.
     * @return a client.
     */
    public static HTTPClient instance(final URL url) {
        return new HTTPClient(url, url.getHost());
    }

    /**
     * Returns client instance for the specified URL and physical host. Use this if the virtual host is different from
     * the physcical host. The original host in the URL will be replaced by the supplied physical host and and the
     * original host will instead be used as a host header.
     *
     * @param url The url.
     * @param physicalHost The physical host.
     *
     * @return a client.
     */
    public static HTTPClient instance(final URL url, final String physicalHost) {
        return new HTTPClient(url, physicalHost);
    }
    
    // Constructors --------------------------------------------------
                                                                                              
    private HTTPClient(final URL url, final String physicalHost) {
        
        try {
            handler = new PhysicalHostStreamHandler(physicalHost);
            u = new URL(url, "", handler);
            id = u.getHost() + ':' + u.getPort();
            
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    // Public --------------------------------------------------------
    
    /**
     * @param path
     * @return
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public Document getXmlDocument(final String path) throws IOException, SAXException {

        loadUrlConnection(path);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();

            final long start = System.nanoTime();

            final Document result = builder.parse(urlConn.getInputStream());

            Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);

            return result;

        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());

        } catch (IOException e) {
            throw interceptIOException(e);

        } finally {

            if (null != urlConn && null != urlConn.getInputStream()) {
                urlConn.getInputStream().close();
            }
            if (null != urlConn) {
                // definitely done with connection now
                urlConn = null;
            }
        }
    }

    /**
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public BufferedInputStream getBufferedStream(final String path) throws IOException {

        loadUrlConnection(path);

        try {
            final long start = System.nanoTime();

            final BufferedInputStream result = new BufferedInputStream(urlConn.getInputStream());

            Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);

            return result;

        } catch (IOException e) {
            throw interceptIOException(e);

        }
    }

    /**
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public BufferedReader getBufferedReader(final String path) throws IOException {

        loadUrlConnection(path);

        try {
            final long start = System.nanoTime();

            final BufferedReader result = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);

            return result;

        } catch (IOException e) {
            throw interceptIOException(e);

        }
    }

    /**
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public long getLastModified(final String path) throws IOException {

        try {
            return loadUrlConnection(path).getLastModified();

        } catch (IOException e) {
            throw interceptIOException(e);

        } finally {
            urlConn = null;
        }
    }

    /**
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public boolean exists(final String path) throws IOException {

        boolean success = false;
        loadUrlConnection(path);

        if (urlConn instanceof HttpURLConnection  || urlConn instanceof JarURLConnection) {
            try {

                if (urlConn instanceof HttpURLConnection) {
                    ((HttpURLConnection)urlConn).setInstanceFollowRedirects(false);
                    ((HttpURLConnection)urlConn).setRequestMethod("HEAD");
                    success = HttpURLConnection.HTTP_OK == ((HttpURLConnection)urlConn).getResponseCode();
                } else {
                    success = urlConn.getContentLength() > 0;
                }
            } catch (IOException e) {
                throw interceptIOException(e);

            } finally {
                urlConn = null;
            }
        } else {
            final File file = new File(path);
            success = file.exists();
        }

        return success;
    }

    /**
     * @param ioe
     * @return
     */
    public IOException interceptIOException(final IOException ioe) {

        final IOException e = interceptIOException(id, urlConn, ioe);

        // definitely done with connection now
        urlConn = null;

        return e;
    }

    /**
     * @param conn
     * @param ioe
     * @return
     */
    public static IOException interceptIOException(
            final URLConnection conn,
            final IOException ioe) {

        final String id = conn.getURL().getHost() + ':'
                + (-1 != conn.getURL().getPort() ? conn.getURL().getPort() : 80);

        return interceptIOException(id, conn, ioe);
    }

    /**
     * @param conn
     * @param time
     */
    public static void addConnectionStatistic(final URLConnection conn, final long time) {

        final String id = conn.getURL().getHost() + ':'
                + (-1 != conn.getURL().getPort() ? conn.getURL().getPort() : 80);

        Statistic.getStatistic(id).addInvocation(time);
    }
        
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    private static IOException interceptIOException(
            final String id,
            final URLConnection urlConn,
            final IOException ioe) {

        if (ioe instanceof SocketTimeoutException) {
            Statistic.getStatistic(id).addReadTimeout();

        } else if (ioe instanceof ConnectException) {
            Statistic.getStatistic(id).addConnectTimeout();

        } else {
            Statistic.getStatistic(id).addFailure();
        }

        // Clean out the error stream. See
        if (urlConn instanceof HttpURLConnection) {
            cleanErrorStream((HttpURLConnection) urlConn);
        }


        return ioe;
    }

    private URLConnection loadUrlConnection(final String path) throws IOException {
        if (null == urlConn) {
            urlConn = new URL(u, path, handler).openConnection();
        }
        return urlConn;
    }

    private static String ensureProtocol(final String host) {
        return host.contains("://") ? host : "http://" + host;
    }

    private static void cleanErrorStream(final HttpURLConnection con) {

        if (null != con.getErrorStream()) {

            final BufferedReader errReader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            final StringBuilder err = new StringBuilder();
            try {
                for (String line = errReader.readLine(); null != line; line = errReader.readLine()) {
                    err.append(line);
                }
                con.getErrorStream().close();

            } catch (IOException ioe) {
                LOG.warn(ioe.getMessage(), ioe);
            }
            LOG.info(err.toString());
        }
    }

    // Inner classes -------------------------------------------------
    
    private static class PhysicalHostStreamHandler extends URLStreamHandler {

        private final String physicalHost;

        public PhysicalHostStreamHandler(final String physicalHost) {
            this.physicalHost = physicalHost;
        }
        
        String getPhysicalHost(){
            return physicalHost;
        }

        protected URLConnection openConnection(final URL u) throws IOException {

            URL url;
            final URLConnection connection;
            final String host;

            if ("jar".equals(u.getProtocol())) {
                // Doesn't work with jar urls?
                // url = new URL(u.getProtocol(), physicalHost, u.getPort(), u.getFile());

                final URL containedURL = new URL(u.getFile());
                final String innerPath = containedURL.toString()
                        .replace("://" + containedURL.getHost(), "://" + physicalHost);
                
                url = new URL("jar:" + innerPath);
                host = containedURL.getHost();
                
                // HACK around http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6270774                
                // XXX !!Danger!! Not at all synchronized! 
                //    Makes a new callback only applicable to this url, 
                //       required that callbacks are not overlapped or repeated!!
                URLJarFile.setCallBack(new URLJarFileCallBackImpl(host));
                // EndOfHACK
                
                // HACK Third solution. Use own URLStreamHandler
                connection = url.openConnection();//new JarURLConnection(url, null);
                // EndOfHACK
                
            } else {
                url = new URL(u.getProtocol(), physicalHost, u.getPort(), u.getFile());
                host = u.getHost();
                connection = url.openConnection();
            }

            connection.addRequestProperty("host", host);
            
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            if (LOG.isTraceEnabled()) {
                LOG.trace(MessageFormat.format(DEBUG_USING_URL, url, host));
            }

            return connection;
        }
        
        /**
         * HACK around http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6270774 
         * XXX !!Danger!! Not at all synchronized!
         * Makes a new callback only applicable to this url, required that callbacks are not overlapped or repeated!!
         **/
        private class URLJarFileCallBackImpl implements URLJarFileCallBack {
            private final String host;

            private URLJarFileCallBackImpl(final String host) {
                this.host = host;
            }

            private int BUF_SIZE = 2048;

            @SuppressWarnings(value = "unchecked")
            public JarFile retrieve(final URL url) throws IOException {

                // next to verbose copy from URLJarFile
                JarFile result = null;

                /* get the stream before asserting privileges */
                final URLConnection connection = url.openConnection();

                connection.addRequestProperty("host", host);
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);

                final InputStream in = connection.getInputStream();

                try {
                    result = (JarFile) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws IOException {
                            OutputStream     out = null;
                            File tmpFile = null;
                            try {
                                tmpFile = File.createTempFile("jar_cache", null);
                                tmpFile.deleteOnExit();
                                out  = new FileOutputStream(tmpFile);
                                int read = 0;
                                byte[] buf = new byte[BUF_SIZE];
                                while ((read = in.read(buf)) != -1) {
                                    out.write(buf, 0, read);
                                }
                                out.close();
                                out = null;
                                return new URLJarFile(tmpFile);

                            } catch (IOException e) {
                                if (tmpFile != null) {
                                    tmpFile.delete();
                                }
                                throw e;
                            } finally {
                                if (in != null) {
                                    in.close();
                                }
                                if (out != null) {
                                    out.close();
                                }
                            }
                        }
                    });
                }catch (PrivilegedActionException pae) {
                    throw (IOException) pae.getException();
                }
                //URLJarFile.setCallBack(null);

                return result;
            }
        }
    }

    private static final class Statistic implements Comparable<Statistic> {


        private static final Map<String, Statistic> STATISTICS = new ConcurrentHashMap<String, Statistic>();

        private static final Logger STATISTICS_LOG = Logger.getLogger(Statistic.class);

        private final String id;
        private long totalTime = 0;
        private long longest = 0;
        private long invocations = 0;
        private volatile long connectTimeouts = 0;
        private volatile long readTimeouts = 0;
        private volatile long failures = 0;
        private static volatile long lastPrint = System.currentTimeMillis() / 60000;
        
        static{
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    printStatistics();
                }
            });
        }

        static Statistic getStatistic(final String id) {

            if (null == STATISTICS.get(id)) {
                STATISTICS.put(id, new Statistic(id));
            }
            return STATISTICS.get(id);
        }

        private Statistic(final String id) {
            this.id = id;
        }

        synchronized void addInvocation(final long time) {

            final long timeMs = (time / 1000000);
            totalTime += timeMs;
            if (timeMs > longest) {
                longest = timeMs;
            }
            ++invocations;

            if (STATISTICS_LOG.isDebugEnabled() && System.currentTimeMillis() / 60000 != lastPrint) {

                printStatistics();
                lastPrint = System.currentTimeMillis() / 60000;
            }
        }

        void addFailure() {
            ++failures;
        }

        void addConnectTimeout() {
            ++connectTimeouts;
        }

        void addReadTimeout() {
            ++readTimeouts;
        }

        private long getAverageInvocationTime() {
            return 0 < invocations ? (totalTime * (long) 1000 / invocations) : 0;
        }

        @Override
        public String toString() {

            return ": " + new DecimalFormat("000,000,000").format(invocations)
                    + " : " + new DecimalFormat("00,000").format(longest)
                    + "ms : " + new DecimalFormat("0,000,000").format(getAverageInvocationTime())
                    + "µs :   " + new DecimalFormat("00,000").format(failures)
                    + " :         " + new DecimalFormat("00,000").format(connectTimeouts)
                    + " : " + new DecimalFormat("00,000").format(readTimeouts)
                    + " <-- " + id;
        }

        public int compareTo(Statistic o) {
            return (int) (o.getAverageInvocationTime() - getAverageInvocationTime());
        }


        private static void printStatistics() {

            final List<Statistic> list = new ArrayList<Statistic>(STATISTICS.values());
            Collections.sort(list);

            final StringBuilder msg = new StringBuilder();
            msg.append("\n------ Printing HTTPClient statistics ------\n"
                    + ": invocations : longest  : average     "
                    + ": failures : connect errors : read timeouts <- client\n");

            for (Statistic stat : list) {
                msg.append(stat.toString() + '\n');
            }
            msg.append("------ ------------------------------ ------");
            STATISTICS_LOG.debug(msg.toString());
        }
    }
}
