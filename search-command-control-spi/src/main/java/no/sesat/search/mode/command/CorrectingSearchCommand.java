/* Copyright (2012) Schibsted ASA
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
 */
package no.sesat.search.mode.command;

import no.sesat.search.mode.config.CorrectingCommandConfig;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

/** @see CorrectingCommandConfig
 *
 * @version $Id$
 */
public interface CorrectingSearchCommand  extends SearchCommand{
        @Override
        CorrectingCommandConfig getSearchConfiguration();

        /** Allows extra initialisation of any newly constructed search command before it is run.
         * For example additional fields to correctionCount can be passed from old command to new.
         *
         * Normal to be a blank implementation simply returning the argument.
         *
         * @param command newly created search command before it is run.
         * @return the newly created command but with extra initialisation performed.
         */
        CorrectingSearchCommand initialiseNewCommand(CorrectingSearchCommand command);

        /**
         * Any implementation must be able to correctQuery(..) if it wishes the command to run again. If this method returns
         * the same query string then the command is not executed again and the current result list deemed desirable.
         * This decision is also influenced by CorrectinCommandConfig.getCorrectingLimit.
         *
         * @param results the result list of the previous executed command
         * @param q the existing query string
         * @return the same query string if result list is desired, an altered query string if command should 'correct'.
         */
        String correctQuery(ResultList<ResultItem> results, String q);
        void setCorrectedQuery(String correctedQuery);
        int getCorrectionCount();
        void setCorrectionCount(int count);

}
