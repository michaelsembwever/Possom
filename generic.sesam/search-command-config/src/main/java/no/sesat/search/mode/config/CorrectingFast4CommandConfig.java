/*
 * Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package no.sesat.search.mode.config;


import no.sesat.search.mode.config.CommandConfig.Controller;


/** Configure a Correcting Fast 4 search command.
 *
 * @version <tt>$Id$</tt>
 */
@Controller("CorrectingFast4SearchCommand")
public class CorrectingFast4CommandConfig extends FastCommandConfig implements CorrectingCommandConfig{

    //private static final Logger LOG = Logger.getLogger(CorrectingFast4CommandConfig.class);

    private int correctingLimit = 1;

    @Override
    public void setCorrectingLimit(final int correctingLimit){
        this.correctingLimit = correctingLimit;
    }

    @Override
    public int getCorrectingLimit(){
        return correctingLimit;
    }
}
