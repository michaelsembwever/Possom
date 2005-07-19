/*
 * Created on Nov 1, 2004
 *
 */
package no.schibstedsok.front.searchportal;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Lars Johansson
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestCase extends junit.framework.TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestCase.class);
	}

	public void testMe() throws Exception {
		
        Calendar cal = new GregorianCalendar(2005, 05, 15, 17, 00);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
        cal = new GregorianCalendar(2005, 05, 15, 17, 10);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
        cal = new GregorianCalendar(2005, 05, 15, 19, 00);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
        cal = new GregorianCalendar(2005, 05, 15, 21, 00);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
        cal = new GregorianCalendar(2005, 05, 15, 23, 00);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
        cal = new GregorianCalendar(2005, 05, 16, 04, 00);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
        cal = new GregorianCalendar(2005, 05, 16, 04, 10);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
        cal = new GregorianCalendar(2005, 05, 16, 8, 00);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
        cal = new GregorianCalendar(2005, 05, 16, 10, 00);
        System.out.print(cal.getTime() + " ");
        System.out.println(cal.getTimeInMillis());
	}


}
