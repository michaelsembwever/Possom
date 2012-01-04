/*
* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
*
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
