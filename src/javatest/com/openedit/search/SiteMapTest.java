/*
 * Created on May 15, 2005
 */
package com.openedit.search;

import org.apache.lucene.document.Document;
import org.openedit.sitesearch.SiteMapModule;
import org.openedit.sitesearch.SiteSearchModule;

import com.openedit.BaseTestCase;
import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;

/**
 * @author cburkey
 *
 */
public class SiteMapTest extends BaseTestCase
{

	public SiteMapTest()
	{
		System.setProperty("oe.root.path", "webapp");

	}
	public void testIndex() throws Exception
	{		
		//Make sure you check /resources/test/search/urls.txt and /webapp/WEB-INF/classes/crawl-url-filter.txt
		SiteMapModule mod = (SiteMapModule)getModule("SiteMapModule");
		WebPageRequest req = getFixture().createPageRequest();
		mod.generateSiteMap(req);
	}
	
	

}
