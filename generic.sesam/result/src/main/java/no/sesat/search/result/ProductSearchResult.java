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
package no.sesat.search.result;

import no.sesat.search.result.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
public class ProductSearchResult implements ProductResult {

    private static final Logger LOG = Logger.getLogger(ProductSearchResult.class);

    private final List<ProductResultItem> listingResults = new ArrayList<ProductResultItem>();
    private final List<ProductResultItem> infoPageResults = new ArrayList<ProductResultItem>();
    
	public void addInfoPageResult(ProductResultItem item) {
		infoPageResults.add(item);
	}

	public void addListingResult(ProductResultItem item) {
		listingResults.add(item);
	}

	public List<ProductResultItem> getInfoPageProducts() {
		return infoPageResults;
	}

	public List<ProductResultItem> getListingProducts() {
		return listingResults;
	}

	public boolean hasInfoPageProducts() {
		return infoPageResults.size() > 0 ? true: false;
	}

	public boolean hasListingProducts() {
		return listingResults.size() > 0 ? true: false;
	}
	
}


