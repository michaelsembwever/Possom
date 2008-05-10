/* Copyright (2007) Schibsted Søk AS
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
 *
 * Suggestion.java
 *
 * Created on 10/05/2007, 13:20:03
 *
 */

package no.sesat.search.result;

import java.io.Serializable;

/**
 *
 *
 * @version $Id$
 */
public interface Suggestion extends Serializable{

    /**
     *
     * @return
     */
    String getOriginal();

    /**
     *
     * @return
     */
    String getSuggestion();

    /**
     *
     * @return
     */
    String getHtmlSuggestion();
}
