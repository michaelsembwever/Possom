/*
 * Copyright (2005-2012) Schibsted ASA
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
package no.sesat.search.run;


import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;


/**
 * The Simple RunningQuery implementation.
 *
 * @version <tt>$Id$</tt>
 */
public class SimpleRunningQueryImpl extends AbstractRunningQuery implements RunningQuery {

   // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SimpleRunningQueryImpl.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** {@inheritDoc} */
    public SimpleRunningQueryImpl(
            final Context cxt,
            final String query) throws SiteKeyedFactoryInstantiationException {

        super(cxt, query);
    }

    // Public --------------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
