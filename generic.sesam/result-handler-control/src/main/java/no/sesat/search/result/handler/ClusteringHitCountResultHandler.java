/* Copyright (2008) Schibsted ASA
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
package no.sesat.search.result.handler;

import java.util.List;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.Modifier;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

import org.apache.log4j.Logger;

/**
 * @see ClusteringHitCountResultHandlerConfig
 *
 * @version $Id$
 */
public class ClusteringHitCountResultHandler implements ResultHandler  {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ClusteringHitCountResultHandler.class);

    // Attributes ----------------------------------------------------

    private final ClusteringHitCountResultHandlerConfig config;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public ClusteringHitCountResultHandler(final ResultHandlerConfig config) {
        this.config = (ClusteringHitCountResultHandlerConfig) config;
    }

    // Public --------------------------------------------------------

    public void handleResult(final Context cxt, final DataModel datamodel) {
        final String cmdId = cxt.getSearchConfiguration().getName();
        final String navId = config.getNavId();

        LOG.debug("cmdId: " + cmdId + ", navId: " + navId);

        final ResultList<? extends ResultItem> searchResult = cxt.getSearchResult();

        LOG.debug("Original hitCount: " + searchResult.getHitCount() + " (" + cmdId + ")");

        if (searchResult.getHitCount() <= 0) {
            LOG.warn("No result, does nothing (" + cmdId + ")");
            return;
        }

        if (!(searchResult instanceof FastSearchResult)) {
            LOG.warn("Result is not a FastSearchResult, does nothing (" + cmdId + ")");
            return;
        }

        final FastSearchResult fres = (FastSearchResult) searchResult;
        final List<Modifier> modifiers = fres.getModifiers(config.getNavId());

        if (modifiers != null && modifiers.size() > 0) {
            // Iterate over all values and sum the counts.
            int newHitCount = 0;

            for (final Modifier modifier : modifiers) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace(modifier.getName() + " -> " + modifier.getCount() + " (" + cmdId + ")");
                }

                newHitCount += modifier.getCount();
            }

            LOG.debug("New hitCount: " + newHitCount  + " (" + cmdId + ")");
            searchResult.setHitCount(newHitCount);
        } else {
            LOG.warn("Navigator '" + config.getNavId() + "' not found, does nothing (" + cmdId + ")");
        }
    }

    // Getters / Setters ---------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
