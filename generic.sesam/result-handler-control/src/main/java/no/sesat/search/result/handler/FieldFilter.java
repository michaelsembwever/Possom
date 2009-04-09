/* Copyright (2007) Schibsted ASA
 *   This file is part of SESAT.
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

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;


/**
 *
 * @version $Id$
 */
public final class FieldFilter implements ResultHandler {

    private final FieldFilterResultHandlerConfig config;

    /**
     *
     * @param config
     */
    public FieldFilter(final ResultHandlerConfig config) {
        this.config = (FieldFilterResultHandlerConfig) config;
    }

    public void handleResult(final Context cxt, final DataModel datamodel) {
        ResultList<ResultItem> searchResult = cxt.getSearchResult();
        filterResult(searchResult);
    }

    private void filterResult(final ResultList<ResultItem> searchResult) {

        for (ResultItem searchResultItem : searchResult.getResults()) {
            if (searchResultItem instanceof ResultList<?>) {
                final ResultList<ResultItem> subResult = (ResultList<ResultItem>)searchResultItem;
                if (subResult != null) {
                    filterResult(subResult);
                }
            }

            if (shouldFilter(searchResultItem)) {

                searchResult.replaceResult(
                        searchResultItem,
                        filter(searchResultItem)
                    );
            }
        }
    }

    private ResultItem filter(ResultItem searchResultItem) {

        for (String removeField : config.getRemoveFieldsArray()) {
            searchResultItem = searchResultItem.addField(removeField, null);
        }
        return searchResultItem;
    }

    private boolean shouldFilter(final ResultItem searchResultItem) {

        final String filterSource = searchResultItem.getField(config.getFilterSrc());
        return filterSource != null && config.getMatchListSet().contains(filterSource.toLowerCase().trim());
    }

}
