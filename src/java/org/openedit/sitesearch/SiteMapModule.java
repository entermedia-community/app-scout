/*
 * Created on May 12, 2005
 */
package org.openedit.sitesearch;

import java.io.File;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.WebPageRequest;
import com.openedit.modules.BaseModule;

/**
 * @author cburkey
 *
 */
public class SiteMapModule extends BaseModule
{
	protected SiteMapper siteMapper;
	
	protected boolean fieldCrawling;
	private static final Log log = LogFactory.getLog(SiteMapModule.class);
	
	public synchronized void generateSiteMap(WebPageRequest inReq) throws Exception
	{
		//TODO: Use a look up tool to support more than one dir
			getSiteMapper().setRootDirectory( getRoot() );
			getSiteMapper().setIndexPath("/search/index/");
			Set links = getSiteMapper().buildMap();
			inReq.putPageValue("links",links);
			

	}

	public SiteMapper getSiteMapper() {
		return siteMapper;
	}

	public void setSiteMapper(SiteMapper siteMapper) {
		this.siteMapper = siteMapper;
	}
	
	
	
	
	
}
