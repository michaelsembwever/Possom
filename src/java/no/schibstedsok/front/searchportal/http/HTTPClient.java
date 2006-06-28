// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.http;

import no.schibstedsok.front.searchportal.InfrastructureException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class HTTPClient {
    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(HttpClient.class);
    private static final String DEBUG_ADDING_CONF = "Adding configuration ";
    
    private static final Map hostConfigurations = new HashMap();

    private static final HttpConnectionManager cMgr = new ConnectionManagerWithoutKeepAlive();
    private HttpClient commonsHttpClient;

    private static final HTTPClient client = new HTTPClient();


    private HTTPClient() {
        
        final HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setStaleCheckingEnabled(true);
        params.setMaxTotalConnections(100);
        cMgr.setParams(params);
        commonsHttpClient = new HttpClient(cMgr);
    }

    public static HTTPClient instance(final String id, final String host, final int port) {
        
        if (!hostConfigurations.containsKey(id)) {
            addHostConfiguration(id, host, port);
        }
        
        return client;
    }

    public HttpMethod executeGet(final String id, final String path) throws IOException {
        HostConfiguration conf = (HostConfiguration) hostConfigurations.get(id);
        HttpMethod method = new GetMethod(path);
        commonsHttpClient.executeMethod(conf, method);
        return method;
    }

    public Document getXmlDocument(final String id, final String path) throws IOException, SAXException {
        HttpMethod method = null;
        try {
            method = executeGet(id, path);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                return builder.parse(method.getResponseBodyAsStream());
            } catch (ParserConfigurationException e) {
                throw new InfrastructureException(e);
            }
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    public void release(final HttpMethod method) {
        method.releaseConnection();
    }

    private synchronized static void addHostConfiguration(final String id, final String host, final int port) {
        
        if (! hostConfigurations.containsKey(id)) {
            HostConfiguration conf = new HostConfiguration();
            LOG.debug(DEBUG_ADDING_CONF + host + ":" + port);
            conf.setHost(host, port, "http");
            cMgr.getParams().setMaxConnectionsPerHost(conf, 1);
            hostConfigurations.put(id, conf);
        }
    }
}
