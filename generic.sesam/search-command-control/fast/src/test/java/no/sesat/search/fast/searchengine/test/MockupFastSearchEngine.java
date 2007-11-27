/* Copyright (2007) Schibsted Søk AS
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

import no.fast.ds.search.*;
import no.sesat.search.result.test.MockupQueryResult;

import java.util.List;
import java.io.IOException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class MockupFastSearchEngine implements IFastSearchEngine {

    private String url;

    public MockupFastSearchEngine(String  url) {
        this.url = url;
    }

    public List getCollections() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCollection() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCluster() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IQueryResult search(String string) throws IOException, SearchEngineException {
        return new MockupQueryResult();
    }

    public IQueryResult search(IQuery iQuery) throws IOException, SearchEngineException {

        MockupQueryResult result = new MockupQueryResult();


        try {
            ISearchParameter navigators = iQuery.getParameter(BaseParameter.NAVIGATORS);

            String navs[] = navigators.getStringValue().split(",");

            for (int i = 0; i < navs.length; i++) {
                String nav = navs[i];
                INavigator navigator = new MockupNavigator(nav);
                result.addNavigator(navigator);
            }

        } catch (NoSuchParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }
}
