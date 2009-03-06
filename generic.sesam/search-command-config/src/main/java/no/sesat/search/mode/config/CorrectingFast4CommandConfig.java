/*
 * Copyright (2005-2008) Schibsted Søk AS
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
 *
 */
package no.sesat.search.mode.config;


import no.sesat.search.mode.config.CommandConfig.Controller;
import org.apache.log4j.Logger;


/** Configure a Correcting Fast 4 search command.
 *
 * @version <tt>$Id$</tt>
 */
@Controller("CorrectingFast4SearchCommand")
public class CorrectingFast4CommandConfig extends FastCommandConfig {

    //private static final Logger LOG = Logger.getLogger(CorrectingFast4CommandConfig.class);

    private int correctingLimit = 1;

    public void setCorrectingLimit(final int correctingLimit){
        this.correctingLimit = correctingLimit;
    }

    public int getCorrectingLimit(){
        return correctingLimit;
    }
}
