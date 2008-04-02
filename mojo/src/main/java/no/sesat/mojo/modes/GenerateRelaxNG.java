package no.sesat.mojo.modes;

import java.util.Iterator;

/**
 * Generator for Relax NG.
 */
public class GenerateRelaxNG extends GenerateSchemaFile {

    /**
     * @param element
     *            Root element.
     * @param name
     *            File name.
     * @param idString
     *            id.
     */
    protected GenerateRelaxNG(final ConfigElement element, final String name, final String idString) {
        super(element, name, idString);
    }

    /**
     * Generate Relax NG.
     */
    @Override
    protected void runImpl() {
        generate(root);
    }

    private void generate(final ConfigElement element) {

        if (element.hasDoc()) {
            final String[] docArray = element.doc.split("\n");
            for (int i = 0; i < docArray.length; i++) {
                println("## " + docArray[i]);
            }
        }

        println("element " + element.name + " {");
        indent();
        if (element.attributes.isEmpty() && element.children.isEmpty()) {
            print(" empty ");
        } else {
            for (final Iterator<ConfigAttribute> iterator = element.attributes.iterator(); iterator.hasNext();) {
                final ConfigAttribute attrib = iterator.next();

                generate(attrib);
                if (iterator.hasNext() || !element.children.isEmpty()) {
                    println(",");
                } else {
                    println("");
                }
            }
        }

        if (!element.children.isEmpty()) {
            println("(");
            for (int i = 0; i < element.children.size(); i++) {
                if (i > 0) {
                    println("|");
                }
                generate(element.children.get(i));
            }
            println(")*");
        }
        unindent();
        println("}*");

    }

    private void generate(final ConfigAttribute attrib) {
        if (attrib.hasDoc()) {
            final String[] docArray = attrib.doc.split("\n");
            for (int i = 0; i < docArray.length; i++) {
                println("## " + docArray[i]);
            }
        }
        print("attribute " + attrib.name + " { text }" + (attrib.required ? "" : "?"));
    }

}
