/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.searchportal.fast.searchengine.test;

import no.fast.ds.search.IModifier;
import no.fast.ds.search.INavigator;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
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
