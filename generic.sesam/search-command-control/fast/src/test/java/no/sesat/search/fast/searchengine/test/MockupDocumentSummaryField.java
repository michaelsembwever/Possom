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
package no.sesat.search.fast.searchengine.test;

import no.fast.ds.search.IDocumentSummaryField;

/**
 *
 * @version <tt>$Revision: 5819 $</tt>
 */
public class MockupDocumentSummaryField implements IDocumentSummaryField {
    private String name;
    private String summary;

    public MockupDocumentSummaryField(String name, String summary) {
        this.name = name;
        this.summary = summary;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public int getType() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
