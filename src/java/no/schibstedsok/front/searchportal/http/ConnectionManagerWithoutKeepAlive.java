package no.schibstedsok.front.searchportal.http;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpConnection;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class ConnectionManagerWithoutKeepAlive extends MultiThreadedHttpConnectionManager {

    public void releaseConnection(HttpConnection httpConnection) {
        httpConnection.close();
        super.releaseConnection(httpConnection);
    }

}
