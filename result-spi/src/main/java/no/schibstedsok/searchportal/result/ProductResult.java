/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.result;

import java.util.List;


/** @deprecated use ResultList instead, subclassing to add infoPageProducts.
 * @version <tt>$Id$</tt>
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 */
public interface ProductResult {

    List<ProductResultItem> getInfoPageProducts();
    List<ProductResultItem> getListingProducts();

    void addInfoPageResult(ProductResultItem item);
    void addListingResult(ProductResultItem item);

    boolean hasInfoPageProducts();
    boolean hasListingProducts();

}
