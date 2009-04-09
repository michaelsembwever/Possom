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

import no.fast.ds.search.INavigator;
import no.fast.ds.search.NavigatorType;
import no.fast.ds.search.IModifier;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

/**
 *
 * @version <tt>$Revision: 5819 $</tt>
 */
public class MockupNavigator implements INavigator {
    private String name;

    private Collection modifiers = new ArrayList();

    public MockupNavigator(String name) {
        this.name = name;

        IModifier modifier1 = new MockupModifier(this, "mod" + name, "mod" + name + "value1");
        IModifier modifier2 = new MockupModifier(this, "mod" + name, "mod" + name + "value2");
        IModifier modifier3 = new MockupModifier(this, "mod" + name, "mod" + name + "value3");

        modifiers.add(modifier1);
        modifiers.add(modifier2);
        modifiers.add(modifier3);
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @deprecated see INavigator's javadoc
     ***/
    public String getModifier() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDisplayName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NavigatorType getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUnit() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getScore() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getHits() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getHitsUsed() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getHitRatio() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getSampleCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getMin() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getMax() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getMean() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getEntropy() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator modifierNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator modifiers() {
        return modifiers.iterator();
    }

    public int modifierCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IModifier getModifier(String string) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public INavigator detach() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDetached() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int compareTo(Object o) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getFrequencyError() {
        return 0;
    }
}
