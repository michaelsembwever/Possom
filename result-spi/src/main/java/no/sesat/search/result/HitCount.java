/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
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
 *
 * HitCount.java
 *
 *
 */

package no.sesat.search.result;
import java.text.NumberFormat;
import java.util.Locale;

/** Utility to present a hit count.
 * Transforms negative hit counts into appropriate strings, eg -1 becomes "?".
 *
 * @version $Id$
 *
 */
public final class HitCount {

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    /**
     * @param hitcount
     * @param locale
     * @return presentable version of the hitcount
     */
    public static String present(final int hitcount, final Locale locale) {


        switch(hitcount){

            case -1:
                return "?";

            default:
                return NumberFormat.getIntegerInstance(locale).format(hitcount);
        }
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of HitCount */
    private HitCount(){}

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------



}
