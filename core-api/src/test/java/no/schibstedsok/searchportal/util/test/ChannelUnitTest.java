/*
 * ChannelUnitTest.java
 *
 * Created on 09 November 2006, 15:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.util.test;
import no.schibstedsok.searchportal.util.Channel;
import org.testng.annotations.Test;

/**
 *
 * @author andersjj
 */
public class ChannelUnitTest extends junit.framework.TestCase {
    
    private final Channel c1 = Channel.newInstance("nrk1", "NRK1", 1);
    private final Channel c2 = Channel.newInstance("tv3", "TV3", 2);
    private final Channel c3 = Channel.newInstance("ztv", "ZTV", 3);
    
    /** Creates a new instance of ChannelUnitTest */
    public ChannelUnitTest(String testName) {
        super(testName);
    }

    @Test
    public void testEqual() {
        assert c1.compareTo(c1) == 0 : "c1 != c1";
    }
    
    @Test
    public void testLessThan() {
        assert c1.compareTo(c2) < 0 : "Excpected negative number";
    }
    
    @Test
    public void testGreaterThan() {
        assert c2.compareTo(c1) > 0 : "Excpected positive number";
    }
    
    @Test
    public void testTransitive() {
        assert c1.compareTo(c2) < 0 : "c1 should be less than c2";
        assert c2.compareTo(c3) < 0 : "c2 should be less than c3";
        assert c1.compareTo(c3) < 0 : "c1 should be less than c3";
    }
}
