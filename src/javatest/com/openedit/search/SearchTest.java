/*
 * Created on May 15, 2005
 */
package com.openedit.search;

import org.openedit.Data;
import org.openedit.sitesearch.SiteSearchModule;

import com.openedit.BaseTestCase;
import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;

/**
 * @author cburkey
 *
 */
public class SearchTest extends BaseTestCase
{

	public SearchTest()
	{
		System.setProperty("oe.root.path", "webapp");

	}
	public void testIndex() throws Exception
	{		
		//Make sure you check /resources/test/search/urls.txt and /webapp/WEB-INF/classes/crawl-url-filter.txt
		SiteSearchModule mod = (SiteSearchModule)getModule("SiteSearchModule");
		WebPageRequest req = getFixture().createPageRequest();
		mod.crawlWebSite(req);
	}
	
	public void testSearch() throws Exception
	{
		SiteSearchModule mod = (SiteSearchModule)getModule("SiteSearchModule");
		WebPageRequest req = getFixture().createPageRequest();
		req.setRequestParameter("query","admin");
		
		mod.search(req);
		
		HitTracker hits = (HitTracker)req.getPageValue("sitehits");
		assertTrue(hits.size() > 0);
		Data hit = (Data)hits.get(0);
		
		//System.out.println(hit.get("title"));
		//System.out.println(hit.get("description"));
		
		String high = hits.highlight(hit, "description");
		System.out.println(high);
		
		//PDF search
//		req.setRequestParameter("query","Licensing");
//		mod.search(req);
//		
//		hits = (HitTracker)req.getPageValue("sitehits");
//		assertEquals(1, hits.size());
		
		
	}

}
