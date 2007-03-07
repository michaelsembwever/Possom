// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.List;


/*
 * @version <tt>$Revision: 1 $</tt>
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 *
 */
public interface ProductResult {

    List<ProductResultItem> getInfoPageProducts();
    List<ProductResultItem> getListingProducts();

    void addInfoPageResult(ProductResultItem item);
    void addListingResult(ProductResultItem item);

    boolean hasInfoPageProducts();
    boolean hasListingProducts();

}
