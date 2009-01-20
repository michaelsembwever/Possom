/*
 * Copyright (2008) Schibsted Søk AS
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

package no.sesat.search.result;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class StringChopperTest {

    @Test
    public void SimpleEmpty() {
        assertEquals(null, StringChopper.chop(null, -1, false));
        assertEquals("", StringChopper.chop("", -1, false));
        assertEquals("", StringChopper.chop("", 0, false));
        assertEquals("", StringChopper.chop("", 1, false));
        assertEquals("", StringChopper.chop("", 2, false));

        assertEquals(null, StringChopper.chop(null, -1, true));
        assertEquals("", StringChopper.chop("", -1, true));
        assertEquals("", StringChopper.chop("", 0, true));
        assertEquals("", StringChopper.chop("", 1, true));
        assertEquals("", StringChopper.chop("", 2, true));
    }

    @Test
    public void Simple() {
        assertEquals("<b>and</b><br/>fisk", StringChopper.chop("<b>and</b><br/>fisk", -1, true));
        assertEquals("<b>and</b><br/>fisk", StringChopper.chop("<b>and</b><br/>fisk", 0, true));
    }

    @Test
    public void SimpleChop() {
        assertEquals("<b>a</b>", StringChopper.chop("<b>and</b><br/>fisk", 1, true));
        assertEquals("<b>an</b>", StringChopper.chop("<b>and</b><br/>fisk", 2, true));
        assertEquals("<b>and</b>", StringChopper.chop("<b>and</b><br/>fisk", 3, true));
        assertEquals("<b>and</b><br/>f", StringChopper.chop("<b>and</b><br/>fisk", 4, true));
        assertEquals("<b>and</b><br/>fi", StringChopper.chop("<b>and</b><br/>fisk", 5, true));
        assertEquals("<b>and</b><br/>fis", StringChopper.chop("<b>and</b><br/>fisk", 6, true));
        assertEquals("<b>and</b><br/>fisk", StringChopper.chop("<b>and</b><br/>fisk", 7, true));
        assertEquals("<b>and</b><br/>fisk", StringChopper.chop("<b>and</b><br/>fisk", 8, true));
        assertEquals("<b>and</b><br/>fisk", StringChopper.chop("<b>and</b><br/>fisk", 1000, true));
    }

    @Test
    public void SimpleChopWholeWord() {
        assertEquals("<b>a...</b>", StringChopper.chop("<b>and</b><br/>fisk", 1, false));
        assertEquals("<b>an...</b>", StringChopper.chop("<b>and</b><br/>fisk", 2, false));
        assertEquals("<b>and...</b>", StringChopper.chop("<b>and</b><br/>fisk", 3, false));
        assertEquals("<b>and</b><br/>...", StringChopper.chop("<b>and</b><br/>fisk", 4, false));
        assertEquals("<b>and</b><br/>...", StringChopper.chop("<b>and</b><br/>fisk", 5, false));
        assertEquals("<b>and</b><br/>...", StringChopper.chop("<b>and</b><br/>fisk", 6, false));
        assertEquals("<b>and</b><br/>fisk", StringChopper.chop("<b>and</b><br/>fisk", 7, false));
        assertEquals("<b>and</b><br/>fisk", StringChopper.chop("<b>and</b><br/>fisk", 8, false));
        assertEquals("<b>and</b><br/>fisk", StringChopper.chop("<b>and</b><br/>fisk", 9, false));
    }

    @Test
    public void Unbalanced() {
        assertEquals("<b>and</b>", StringChopper.chop("<b>and", -1, true));
        assertEquals("fisk<b>and</b>", StringChopper.chop("fisk<b>and", -1, true));
    }

    @Test
    public void CDATA() {
        assertEquals("<b>and<![CDATA[ <xml>&& fisk]]> <br/>wonk</b>", StringChopper.chop("<b>and<![CDATA[ <xml>&& fisk]]> <br/>wonk", -1, true));
    }

    @Test
    public void CDATAChop() {
        assertEquals("<b>and<![CDATA[ <xm]]></b>", StringChopper.chop("<b>and<![CDATA[ <xml>&& fisk]]> <br/>wonk", 7, true));
    }

    @Test
    public void CDATAChopWholeWord() {
        assertEquals("<b>and<![CDATA[ <xml>&& ...]]></b>", StringChopper.chop("<b>and<![CDATA[ <xml>&& fisk]]> <br/>wonk", 14, false));
    }

    @Test
    public void Comments() {
        assertEquals("<!-- ups--><b>and<![CDATA[ <xml>&& ...]]></b>", StringChopper.chop("<!-- ups--><b>and<![CDATA[ <xml>&& fisk]]> <br/>wonk", 14, false));
        assertEquals("<b>and<!-- ups--><![CDATA[ <xm]]></b>", StringChopper.chop("<b>and<!-- ups--><![CDATA[ <xml>&& fisk]]><!-- ups--> <br/>wonk<!-- ups-->", 7, true));
        assertEquals("<b>and<!-- ups--><![CDATA[ <xml>&& fisk]]> <br/>wonk</b>", StringChopper.chop("<b>and<!-- ups--><![CDATA[ <xml>&& fisk]]> <br/>wonk", -1, true));
        assertEquals("<b>a<!-- ups-->nd</b>", StringChopper.chop("<b>a<!-- ups-->nd", -1, true));
        assertEquals("fi<!-- ups-->s<!-- ups-->k<b>an<!-- ups-->d<!-- ups--></b>", StringChopper.chop("fi<!-- ups-->s<!-- ups-->k<b>an<!-- ups-->d<!-- ups-->", -1, true));
    }

    @Test
    public void Directive() {
      assertEquals("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><attrib name=\"allowed_types\"> fisk</attrib>",
                StringChopper.chop("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><attrib name=\"allowed_types\"> fisk", -1, false));
    }

    /** currently not supported
    @Test
    public void Doctype() {
        assertEquals("<!DOCTYPE html\nPUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\";>",
                StringChopper.chop("<!DOCTYPE html\nPUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\";>", -1));
    }
     */

    @Test
    public void Morten() {
        assertEquals("<attrib name=\"allowed_types\" type=\"list-string\"><member> text/html </member><member> text/plain </member></attrib>",
                StringChopper.chop("<attrib name=\"allowed_types\" type=\"list-string\"><member> text/html </member><member> text/plain </member>", -1, false));
        assertEquals("<attrib name=\"allowed_types\" value=\"morten er kool\"/>",
                StringChopper.chop("<attrib name=\"allowed_types\" value=\"morten er kool\"/>", -1, false));
        assertEquals("<attrib name=\"allowed_types\"> fisk</attrib>",
                StringChopper.chop("<attrib name=\"allowed_types\"> fisk", -1, false));
    }

    @Test
    public void Haavard() {
        assertEquals("<div>wonk</div>...",
                StringChopper.chop("<div>wonk</div>tonk</div>", 5, false));
    }
}