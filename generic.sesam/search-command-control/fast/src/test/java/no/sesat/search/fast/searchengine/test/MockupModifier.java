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

import no.fast.ds.search.IModifier;
import no.fast.ds.search.INavigator;

/**
 *
 * @version <tt>$Revision: 5819 $</tt>
 */
public class MockupModifier implements IModifier {
    private INavigator navigator;
    private String name;
    private String value;

    public MockupModifier(INavigator navigator, String name, String value) {
        this.navigator = navigator;
        this.name = name;
        this.value = value;
    }

    public INavigator getNavigator() {
        return navigator;
    }

    public String getAttribute() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getDocumentRatio() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IModifier detach() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDetached() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
