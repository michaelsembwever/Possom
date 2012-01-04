/*
 * Copyright (2006-2012) Schibsted ASA
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
 */
package no.sesat.search.mode.command;

/**
 * Helper base implementation for search commands that are RESTful.
 *
 * The RESTful server is defined through:
 * host: AbstractRestfulSearchConfiguration.getHost()
 * port: AbstractRestfulSearchConfiguration.getPort()
 *
 * @version $Id$
 */
public abstract class AbstractRestfulSearchCommand extends AbstractSearchCommand{


    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(AbstractXmlSearchCommand.class);

    // Attributes ----------------------------------------------------

    private Restful restful;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------


    /**
     * Create new xml based command.
     *
     * @param cxt The context to execute in.
     */
    protected AbstractRestfulSearchCommand(final Context cxt) {
        super(cxt);
    }

    // Public --------------------------------------------------------

    public String createRequestURL() {

        return restful.createRequestURL();
    }



    // Protected -----------------------------------------------------

    protected final Restful getRestful(){
        return restful;
    }

    protected final void setRestful(final Restful restful){
        this.restful = restful;
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
