// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

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


