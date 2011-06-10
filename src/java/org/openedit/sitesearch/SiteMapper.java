package org.openedit.sitesearch;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.data.lucene.BaseLuceneSearcher;
import org.openedit.data.lucene.LuceneHitTracker;
import org.openedit.data.lucene.StemmerAnalyzer;
import org.openedit.links.Link;
import org.openedit.repository.filesystem.StringItem;

import com.openedit.ModuleManager;
import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.config.XMLConfiguration;
import com.openedit.hittracker.HitTracker;
import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;
import com.openedit.util.PathUtilities;
import com.openedit.util.XmlUtil;

public class SiteMapper extends BaseLuceneSearcher
{
	//Kinda cheating.  Don't really NEED to be a baseLuceneSearch, but wanted the analyzers.
	private static final Log log = LogFactory.getLog(SiteMapper.class);
	protected ModuleManager fieldModuleManager;
	protected PageManager fieldPageManager;
	
	protected Map fieldParsers;
	
	public Analyzer getAnalyzer()
	{
		if (fieldAnalyzer == null) {
			fieldAnalyzer = new StemmerAnalyzer();
		}
		return fieldAnalyzer;
	}

	public Set buildMap() throws OpenEditException
	{
		//Loop over site and search for all content I can find
		//TODO: Add PDF 
		File config = new File( getRootDirectory(),"search.xml");
		if( !config.exists() )
		{
			throw new OpenEditException("No urls specified in " + config.getAbsolutePath() );
		}		
		
		try
		{
			Set links = newMap(config);
			return links;
		}
		catch ( Exception ex)
		{
			throw new OpenEditException(ex);
		}
		
	}

	private Set newMap(File config) throws Exception
	{
			Status status = new Status();
			Element root = new XmlUtil().getXml(config,"UTF-8");
			for (Iterator iterator = root.elementIterator("allow"); iterator.hasNext();)
			{
				Element site = (Element) iterator.next();
				status.getAllowedSites().add(site.getTextTrim());
			}
			String start = root.elementText("starturl");
			Link startlink = new Link();
			startlink.setPath(start);
			map(startlink,status);
			Set finalList = status.getVisitedHrefs();
			
			Element outputRoot = DocumentHelper.createDocument().addElement("urlset");
			outputRoot.addAttribute("xmlns", "http://www.google.com/schemas/sitemap/0.9");
			for (Iterator iterator = finalList.iterator(); iterator.hasNext();) {
				String href = (String) iterator.next();
				Element url = outputRoot.addElement("url");
				Element loc = url.addElement("loc");
				loc.setText(href);				
			}
			
			Page outputPage = getPageManager().getPage("/sitemap.xml");
			StringWriter out = new StringWriter();
			new XmlUtil().saveXml(outputRoot, out, "UTF-8");
			StringItem string = new StringItem(outputPage.getPath(),out.toString(),outputPage.getCharacterEncoding());
			string.setMakeVersion(false);
			outputPage.setContentItem(string);
			getPageManager().putPage(outputPage);
		return finalList;	
		
	}

	protected void map(Link inLink, Status inStatus) throws Exception
	{
		String inUrl = inLink.getPath();
		
		inStatus.addVisitedUrl(inUrl);
		Content content = new Content();
		content.setUrl(inLink);
		Parse results = getParser(inUrl).getParse(content);
		if( results == null)
		{
			log.error("No content on " + inUrl);
			return;
		}
			//Now look for links not yet visited
		List links = (List)results.getList("links");
		int count = 0;
		for (Iterator iterator = links.iterator(); iterator.hasNext();)
		{
			Link  href = (Link) iterator.next();
			if( inStatus.followHref(href.getPath()) )
			{
				try
				{
					count++;
					map(href,inStatus);
				}
				catch ( Exception ex)
				{
					log.error("Could not parse " + href + " " + ex);
				}
			}
		}
		if( count > 5)
		{
			log.info("sleep");
			Thread.sleep(500);
		}
	}
	
	protected Parser getParser(String inPath)
	{
		String type = null;//
		
		if( inPath.indexOf("." ) > -1 )
		{
			type = PathUtilities.extractPageType(inPath);
		}
		else
		{
			type = "html";
		}
		if( type != null)
		{
			type = type.toLowerCase();
			if( type.equals("htm"))
			{
				type = "html";
			}
			type = type + "Parser";
			Parser parser = (Parser)getParsers().get(type);
			if( parser == null)
			{
				parser = (Parser)getModuleManager().getBean(type);
				if( parser == null)
				{
					log.error("No parser for " + inPath + " using html");
					parser = (Parser)getModuleManager().getBean("htmlParser");
				}
				getParsers().put(type, parser);
			}
			return parser;
		}
		log.error("No parser for " + inPath);
		return null;
	}
	
	protected Map getParsers()
	{
		if( fieldParsers == null)
		{
			fieldParsers = new HashMap();
		}
		return fieldParsers;
	}

	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}

	public PageManager getPageManager() {
		return fieldPageManager;
	}

	public void setPageManager(PageManager pageManager) {
		this.fieldPageManager = pageManager;
	}


	public void reIndexAll(IndexWriter writer) 
	{
		//do nothing
		
	}

	public HitTracker getAllHits(WebPageRequest inReq) 
	{
		//TODO: What to do with this?
		return new LuceneHitTracker();
	}


}
