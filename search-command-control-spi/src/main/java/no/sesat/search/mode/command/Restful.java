/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sesat.search.mode.command;

import java.io.BufferedReader;
import java.io.IOException;

/** SearchCommands that are RESTful should implement this behaviour.
 * http://en.wikipedia.org/wiki/Representational_State_Transfer
 *
 * @version $Id$
 */
public interface Restful {

    /** A RESTful service requires a URL.
     * The resource part of the URL is usually constant to the particular command instance,
     *  but each search creates a seperate URL.
     *
     * @return the URL to use to the RESTful service.
     */
    String createRequestURL();

    /** Obtain a BufferedReader, in the given encoding, of the RESTful result.
     * Makes the presumption that the RESTful service returns an ascii and not binary response.
     *
     * @param encoding
     * @return
     * @throws java.io.IOException
     */
    BufferedReader getHttpReader(String encoding) throws IOException;

}
