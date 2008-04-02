package no.sesat.mojo.modes;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generator for DTD.
 *
 */
public class GenerateDTD extends GenerateSchemaFile {
    private final Set<String> written = new TreeSet<String>();

    /**
     * @param element
     *            Root element.
     * @param name
     *            File name.
     * @param idString
     *            id.
     */
    public GenerateDTD(final ConfigElement element, final String name, final String idString) {
        super(element, name, idString);
    }

    /**
     * Generate DTD.
     */
    @Override
    public void runImpl() {
        println("<?xml version='1.0' encoding='UTF-8'?>\n");
        println("<!-- " + id + " -->");
        generate(root);
    }

    private void generate(final ConfigElement element) {
        if (written.add(element.name)) {

            if (element.hasDoc()) {
                println("<!-- " + element.doc + " -->");
            }

            print("<!ELEMENT " + element.name);
            if (element.children.isEmpty()) {
                print(" EMPTY");
            } else {
                print(" (");
                for (int i = 0; i < element.children.size(); i++) {
                    if (i > 0) {
                        print("|");
                    }
                    print(element.children.get(i).name);
                }
                print(")*");
            }
            println(">");

            generate(element.attributes);
            printlnI("<!ATTLIST " + element.name + " ");
            for (final Iterator<ConfigAttribute> iterator = element.attributes.iterator(); iterator.hasNext();) {
                final ConfigAttribute attrib = iterator.next();
                print(attrib.name + " ");
                generate(attrib);
            }
            printlnU(">");

            for (int i = 0; i < element.children.size(); i++) {
                generate(element.children.get(i));
            }
        }
    }

    private void generate(final ConfigAttribute attrib) {
        println(attrib.type + " " + (attrib.required ? "#REQUIRED" : "#IMPLIED"));
    }

    private void generate(final List<ConfigAttribute> attributes) {
        println("<!--");
        for (final Iterator<ConfigAttribute> iterator = attributes.iterator(); iterator.hasNext();) {
            final ConfigAttribute attrib = iterator.next();
            print("   @attr " + attrib.name);
            if (attrib.hasDoc()) {
                print(" " + attrib.doc);
            }
            println("");
        }
        println("-->");
    }
}
