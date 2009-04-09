/* Copyright (2007) Schibsted ASA
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
package no.sesat.search.fast.searchengine.test;

import no.fast.ds.search.IDocumentSummary;
import no.fast.ds.search.IDocumentSummaryField;

import java.util.Iterator;

/**
 *
 * @version <tt>$Revision: 5819 $</tt>
 */
public class MockupDocumentSummary implements IDocumentSummary {

    int resultNo;

    public MockupDocumentSummary(int i) {
        resultNo = i;
    }

    public int getDocNo() {
        return 0;
    }

    public int fieldCount() {
        return 0;
    }

    public IDocumentSummaryField getSummaryField(int i) {
        return null;
    }

    public IDocumentSummaryField getSummaryField(String string) {
        return new MockupDocumentSummaryField(string, "result number " + resultNo);
    }

    public Iterator summaryFields() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
