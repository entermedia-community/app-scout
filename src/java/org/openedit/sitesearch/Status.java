package org.openedit.sitesearch;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.util.PathUtilities;

public class Status
{
	private static final Log log = LogFactory.getLog(Status.class);
	Set fieldVisitedHrefs;
	Set fieldAllowedSites;
	
	String filter = ".jsp .php .html .htm .pdf"; //TODO: Externalize this into search.xml file
	
	public Set getVisitedHrefs()
	{
		if (fieldVisitedHrefs == null)
		{
			fieldVisitedHrefs = new HashSet();
		}
		return fieldVisitedHrefs;
	}
	public void setVisitedHrefs(Set inVisitedHrefs)
	{
		fieldVisitedHrefs = inVisitedHrefs;
	}
	public Set getAllowedSites()
	{
		if (fieldAllowedSites == null)
		{
			fieldAllowedSites = new HashSet();
		}
		return fieldAllowedSites;
	}
	public void setAllowedSites(Set inAllowedSites)
	{
		fieldAllowedSites = inAllowedSites;
	}
	public boolean followHref(String inHref)
	{
		if( getVisitedHrefs().contains(inHref))
		{
			return false;
		}
		//TODO: Use Mime Type to figure this out. It works for OpenEdit sites for now
		if( inHref.indexOf(".") > -1)
		{
			String ext = PathUtilities.extractPageType(inHref);
			if( ext != null && filter.indexOf(ext.toLowerCase()) == -1)
			{
				return false;
			}
		}	
		for (Iterator iterator = getAllowedSites().iterator(); iterator.hasNext();)
		{
			String root = (String) iterator.next();
			if( inHref.startsWith(root))
			{
				return true;
			}
		}
		log.info("Skip external "+ inHref);
		return false;
	}
	public void addVisitedUrl(String inUrl)
	{
		if( inUrl.endsWith("/"))
		{
			getVisitedHrefs().add(inUrl + "index.html");
			getVisitedHrefs().add(inUrl + "index.htm");
		} else if( inUrl.endsWith("/index.html") || inUrl.endsWith("/index.html"))
		{
			getVisitedHrefs().add(PathUtilities.extractDirectoryPath(inUrl));
		}
		getVisitedHrefs().add(inUrl);
	}
	
}
