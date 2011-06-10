/*
 * Created on May 12, 2005
 */
package org.openedit.sitesearch;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.data.SearcherManager;
import org.openedit.data.lucene.LuceneHitTracker;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.modules.BaseModule;

/**
 * @author cburkey
 *
 */
public class SiteSearchModule extends BaseModule
{
	protected SearcherManager fieldSearcherManager;
	
	protected boolean fieldCrawling;
	private static final Log log = LogFactory.getLog(SiteSearchModule.class);
	
	public synchronized void crawlWebSite(WebPageRequest inReq) throws Exception
	{
		//TODO: Use a look up tool to support more than one dir			
			getSiteSearcher().reIndexAll();
			getPageManager().clearCache(); //TODO: Do this along the way. Or add a timer to do this. Or a bounds check
	}
	
	
	public void search(WebPageRequest inReq) throws Exception
	{
		String queryString = inReq.getRequestParameter("query");
		if (queryString == null || queryString.trim().length() == 0)
		{
			return;
		}
		queryString = queryString.trim();  
		// get query from inReq
		SearchQuery query = new SearchQuery();
		query.addStartsWith("description",queryString);
		query.putInput("query", queryString);
		HitTracker tracker = getSiteSearcher().cachedSearch(inReq, query);
		tracker.setSearchQuery(query);
		inReq.putSessionValue("sitehits",tracker);
	}

	public void loadPageOfSearch(WebPageRequest inPageRequest) throws Exception
	{
		String page = inPageRequest.getRequestParameter("page");
	
		if (page != null && page.length() < 3)
		{
			LuceneHitTracker tracker = (LuceneHitTracker) inPageRequest.getSessionValue("sitehits");
			if (tracker != null)
			{
				int jumpToPage = Integer.parseInt(page);
				if (jumpToPage <= tracker.getTotalPages() && jumpToPage > 0)
				{
					tracker.setPage(jumpToPage);
				}
				else
				{
					tracker.setPage(1);
				}
			}
			else
			{
				//log.error("No search found to turn page on " + inPageRequest.getPathUrl());
			}
		}
	
	}
	//TODO: Add page of hits
	public SiteSearcher getSiteSearcher()
	{
		return (SiteSearcher)getSearcherManager().getSearcher("search", "site");
	}


	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}


	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}
}
