// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.command.PicSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class PicSearchConfiguration extends AbstractSearchConfiguration {

    public PicSearchConfiguration(){
        super(null);
    }
    
    public PicSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    /**
     * Holds value of property queryServerHost.
     */
    private String queryServerHost;

    /**
     * Getter for property queryServerUrl.
     * @return Value of property queryServerUrl.
     */
    public String getQueryServerHost() {
        return this.queryServerHost;
    }

    /**
     * Setter for property queryServerURL.
     * @param queryServerURL New value of property queryServerURL.
     */
    public void setQueryServerHost(final String queryServerURL) {
    }

    /**
     * Holds value of property queryServerPort.
     */
    private int queryServerPort;

    /**
     * Getter for property queryServerPort.
     * @return Value of property queryServerPort.
     */
    public int getQueryServerPort() {
        return this.queryServerPort;
    }

    /**
     * Setter for property queryServerPort.
     * @param queryServerPort New value of property queryServerPort.
     */
    public void setQueryServerPort(final int queryServerPort) {
        this.queryServerPort = queryServerPort;
    }

}
