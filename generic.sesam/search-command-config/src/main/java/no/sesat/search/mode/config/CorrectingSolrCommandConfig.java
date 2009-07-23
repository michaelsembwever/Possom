/*
 * Copyright (2005-2009) Schibsted ASA
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


/** Configure a Correcting Solr search command.
 *
 * @version <tt>$Id$</tt>
 */
@Controller("CorrectingSolrSearchCommand")
public class CorrectingSolrCommandConfig extends SolrCommandConfig implements CorrectingCommandConfig{

    //private static final Logger LOG = Logger.getLogger(CorrectingSolrCommandConfig.class);

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
