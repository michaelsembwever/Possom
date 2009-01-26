/* Copyright (2009) Schibsted Søk AS
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

package no.sesat.search.view.velocity;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.view.config.SearchTab.Layout;
import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

/** Convert sections (datamodel.includes) of the context's DataModel into a JSON object.
 * This allows client javascript to use the datamodel exactly like JSP and velocity templates do.
 *
 * <br/><br/>
 *
 * For example, adding to any tab (in views.xml) the following
 * <pre>
 *       <layout id="json" main="datamodel-json.vm" >
 *           <property key="datamodel.includes" value="searches,navigation,query"/>
 *           <property key="datamodel.includes" value="configuration"/>
 *       </layout>
 * </pre>
 *
 * allows the javascript to access after a request to this tab with the addition URL parameter "layout=json"
 * to access datamodel.getSearches() datamodel.getNavigation() datamodel.getQuery()
 * but will exclude any bean properties, at any level, named "configuration".
 *
 * Or what to serialise can be passed into the directive as named argument pairs.
 * For example: <br/>
 * #jsonDataModel('yellow' $datamodel.getSearch('yellow').results 'yellow'  $datamodel.getSearch('yellow').results)
 *
 * <br/><br/> Add prettyJson=true to the url to get the output in pretty format.
 *
 * <br/><br/>This class was inspired by Karl Øie's work in MapJSONDirective.java
 *
 * @version $Id$
 */
public final class JsonDataModelDirective extends AbstractDirective {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(JsonDataModelDirective.class);

    private static final String NAME = "jsonDataModel";

    private static final String DATAMODEL_INCLUDES = "datamodel.includes";
    private static final String DATAMODEL_EXCLUDES = "datamodel.excludes";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public JsonDataModelDirective() {}

    // Public --------------------------------------------------------

    public String getName() {
        return NAME;
    }

    public int getType() {
        return LINE;
    }

    public boolean render(
            final InternalContextAdapter context,
            final Writer writer,
            final Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (0 <= node.jjtGetNumChildren()) {

            final JsonConfig config = new JsonConfig();
            // Use NOPROP to indicate any cyclic references
            config.setCycleDetectionStrategy(CycleDetectionStrategy.NOPROP);
            // Ignore all transient bean properties
            config.setIgnoreTransientFields(true);

            final String[] excludes = null != ((Layout)context.get("layout")).getProperty(DATAMODEL_EXCLUDES)
                    ? ((Layout)context.get("layout")).getProperty(DATAMODEL_EXCLUDES).split(",")
                    : new String[0];

            final String[] fullExcludes = new String[excludes.length + 2];
            // Ignore all of the junkYard (it's deprecated)
            fullExcludes[0] = "junkYard";
            //  and all toString methods.
            fullExcludes[1] = "string";
            System.arraycopy(excludes, 0, fullExcludes, 2, excludes.length);
            config.setExcludes(fullExcludes);

            final String[] includes = null != ((Layout)context.get("layout")).getProperty(DATAMODEL_INCLUDES)
                    ? ((Layout)context.get("layout")).getProperty(DATAMODEL_INCLUDES).split(",")
                    : new String[0];

            final DataModel datamodel = getDataModel(context);

            final Map<String,Object> map = new HashMap<String,Object>();

            for(String include : includes){

                try {

                    map.put(include, getDataModelInclude(datamodel, include));

                } catch (IntrospectionException ex) {
                    LOG.error("failed to add include " + include, ex);
                } catch (IllegalAccessException ex) {
                    LOG.error("failed to add include " + include, ex);
                } catch (IllegalArgumentException ex) {
                    LOG.error("failed to add include " + include, ex);
                } catch (InvocationTargetException ex) {
                    LOG.error("failed to add include " + include, ex);
                }
            }

            for(int i = 0; i + 1 < node.jjtGetNumChildren(); i+=2){
                map.put(getArgument(context, node, i), getObjectArgument(context, node, i+1));
            }

            assert 0 < map.size() : "No datamodel.includes included";

            if(null != datamodel.getParameters().getValue("prettyJson")){
                writer.write(JSONSerializer.toJSON(map, config).toString(3));
            }else{
                JSONSerializer.toJSON(map, config).write(writer);
            }


        }else{
            LOG.error("#" + getName() + " - wrong number of arguments");
            return false;
        }

        return true;
    }


    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private Object getDataModelInclude(final DataModel datamodel, final String include)
            throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

        Object dataObject = datamodel;

        for(int firstDot = 0
                ; -1 != firstDot
                ; firstDot = include.indexOf('.', firstDot + 1)) {

            final int secondDot = include.indexOf('.', firstDot + 1);

            final String beanName
                    = include.substring(firstDot, -1 != secondDot ? secondDot : include.length());

            final BeanInfo beanInfo = Introspector.getBeanInfo(dataObject.getClass());

            for(PropertyDescriptor prop : beanInfo.getPropertyDescriptors()){
                if(beanName.equals(prop.getName())){
                    dataObject = prop.getReadMethod().invoke(dataObject);
                    break;
                }
            }
        }
        return dataObject;
    }

    // Inner classes -------------------------------------------------

}
