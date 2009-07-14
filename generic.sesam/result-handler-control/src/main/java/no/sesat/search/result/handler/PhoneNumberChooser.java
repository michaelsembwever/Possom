/* Copyright (2006-2009) Schibsted ASA
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

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;

/**
 * @deprecated Sesam.no katalog specific. Will be removed soon.
 * Use FieldChooser instead with from="yphovedtelefon,ypandretelefoner,ypmobiltelefon" target="ypanynumber".
 *
 * @version <tt>$Id$</tt>
 */
public final class PhoneNumberChooser implements ResultHandler {

    private final PhoneNumberChooserResultHandlerConfig config;

    /**
     *
     * @param config
     */
    public PhoneNumberChooser(final ResultHandlerConfig config){
        this.config = (PhoneNumberChooserResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final ResultItem item : cxt.getSearchResult().getResults()) {
            final String phoneNumber = item.getField("yphovedtelefon");
            final String otherNumbers = item.getField("ypandretelefoner");
            final String mobileNumber = item.getField("ypmobiltelefon");

            String chosenNumber = null;

            if (phoneNumber != null) {
                chosenNumber = phoneNumber;
            } else {
                if (otherNumbers != null) {
                    final String[]  numbers = otherNumbers.split(";");

                    if (numbers.length > 0) {
                        chosenNumber = numbers[0];
                    }
                } else {
                    chosenNumber = mobileNumber;
                }
            }

            cxt.getSearchResult().replaceResult(item, item.addField("ypanynumber", chosenNumber));
        }
    }
}