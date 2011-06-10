package com.openedit.search;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.openedit.search");
		//$JUnit-BEGIN$
		suite.addTestSuite(SearchTest.class);
		//$JUnit-END$
		return suite;
	}

}
