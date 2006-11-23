package no.schibstedsok.searchportal.view.velocity;

import java.util.List;

import junit.framework.TestCase;



public class ShareHoldersDirectiveTest extends TestCase {

	public ShareHoldersDirectiveTest(String testName) {
		super(testName);
	}
	
	/**
	 * Test the parse 3 share holders
	 */
	public void testParsing() {

        String ypRoles = "#aksjonaer0##sepnl#\n " +  
        "#bold#Navn#sep#Eierandel i %#sep#Antall aksjer#sepnl#\n" +
        "STENSENTERET AS#id#2703596#sep#50#sep#100#sepnl#\n" +  
        "ARILD C. GUSTAVSEN#id##sep#25#sep#50#sepnl#\n" +
        "INGER A. O. GUSTAVSEN#id##sep#25#sep#50#sepnl#\n\n\n";		
        
        ShareHoldersDirective shd = new ShareHoldersDirective();
        List shareHolders = shd.parse(ypRoles);
        assert shareHolders.size() == 3;
        
	}

	/**
	 * Test parse null string
	 */

	public void testParsing2() {
		ShareHoldersDirective shd = new ShareHoldersDirective();
		List shareHolders = shd.parse(null);
		assert shareHolders.size () == 0;

	}

	/** 
	 * Test parse empty string
	 */

	public void testParstingEmpty () {
		ShareHoldersDirective shd = new ShareHoldersDirective();
		List shareHolders = shd.parse("");
		assert shareHolders.size () == 0;
	}
	
	/**
	 * Try to parse crappy data
	 *
	 */
	public void testParseRandomString() {
        String ypRoles = "#aksjonaer0##sepnl#\n " +  
        "#bold#Navn#sep#Eierandel i %#sep#Antall aksjer#sepnl#\n" +
        "STENSENTERET AS#id#27035950#sep#100#sepnl#\n" +  
        "ARILD C. GUSTAVSEN#id##sep#25#sep#50" +
        "INGER A. O. GUSTAVSEN#sep#25#sep#50#sepnl#\n\n\n";		
		ShareHoldersDirective shd = new ShareHoldersDirective();
		List shareHolders = shd.parse(ypRoles);
		assert shareHolders.size () == 0;
		
	}
}
