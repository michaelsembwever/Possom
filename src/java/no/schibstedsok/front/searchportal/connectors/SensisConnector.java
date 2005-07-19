/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.connectors;

import no.schibstedsok.front.searchportal.connectors.interfaces.Connector;


/**
 * A SensisConnector.
 * 
 * @deprecated Use Fast connector (Sensis is a Fast index).
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class SensisConnector extends FastConnector {

    public static SensisConnector instance = null;

    public static Connector getInstance() {
        if(instance == null)
            instance = new SensisConnector();
        return instance;
    }

    /**
     * 
     */
    private SensisConnector() {
        super();
    }
}