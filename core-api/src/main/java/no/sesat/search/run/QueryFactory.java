/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.run;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;

/**
 * QueryFactory is part of no.sesat.search.query.
 *
 * Use QueryFactory to create a new RunningQuery instance. The RunningQuery
 * should contain all information on how to search in fast.
 *
 *
 * @version $Id$
 */
public abstract class QueryFactory {

    private static final Logger LOG = Logger.getLogger(QueryFactory.class);
    private static QueryFactory instance;

    /**
     * Create a new instance of QueryFactory.
     * @return instance
     */
    public static QueryFactory getInstance() {
        if (instance == null) {

            if (LOG.isInfoEnabled()) {
                LOG.info("Creating new QueryFactory instance");
            }
            instance = new QueryFactoryImpl();
        }
        return instance;
    }

    /**
     * Create a new instance of running query. The implementation can
     * be RunningWebQuery, RunningAdvWebQuery for example.
     * @param mode with SearchConfiguration
     * @param request with parameters
     * @param response
     * @return instance of RunningQuery
     */
    public abstract RunningQuery createQuery(
                             RunningQuery.Context cxt,
                             HttpServletRequest request,
                             HttpServletResponse response) throws SiteKeyedFactoryInstantiationException;

}
