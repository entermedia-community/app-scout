package org.openedit.sitesearch;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.dom4j.Element;
import org.openedit.data.lucene.BaseLuceneSearcher;
import org.openedit.data.lucene.LuceneHitTracker;
import org.openedit.data.lucene.StemmerAnalyzer;
import org.openedit.links.Link;

import com.openedit.ModuleManager;
import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;
import com.openedit.util.FileUtils;
import com.openedit.util.PathUtilities;
import com.openedit.util.XmlUtil;

public class SiteSearcher extends BaseLuceneSearcher
{
	private static final Log log = LogFactory.getLog(SiteSearcher.class);
	protected ModuleManager fieldModuleManager;
	
	protected Map fieldParsers;
	
	public Analyzer getAnalyzer()
	{
		if (fieldAnalyzer == null) {
			fieldAnalyzer = new StemmerAnalyzer();
		}
		return fieldAnalyzer;
	}
	public void reIndexAll(IndexWriter writer)
	{
		//Loop over site and search for all content I can find
		//TODO: Add PDF 
		File config = new File( getRootDirectory(), "/" + getCatalogId() + "/search.xml");
		if( !config.exists() )
		{
			throw new OpenEditException("No urls specified in " + config.getAbsolutePath() );
		}
		try
		{
			writer.setMergeFactor(100);
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
			index(startlink,status, writer);
			writer.optimize();
			log.info("Indexed " + status.getVisitedHrefs().size() + " links");
		}
		catch( Exception ex)
		{
			throw new OpenEditException(ex);
		}
	}

	protected void index(Link inLink, Status inStatus, IndexWriter inWriter) throws Exception
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
		Document doc = new Document();
		doc.add(new Field("href", inUrl, Field.Store.YES, Field.Index.TOKENIZED));
		
		String title = results.getTitle();
		if( title != null)
		{
			doc.add(new Field("title", title, Field.Store.YES, Field.Index.TOKENIZED));
		}
//		if( keywords != null)
//		{
//			doc.add(new Field("keywords", keywords, Field.Store.YES, Field.Index.TOKENIZED));
//		}
		String summary = (String)results.get("summary");
		String body = results.getText();
		if( summary != null)
		{
			doc.add(new Field("summary", summary, Field.Store.YES, Field.Index.TOKENIZED));
		}
		StringBuffer  all = new StringBuffer();//= title +  " " + keywords + " " + summary + " " + body;
		if( title != null)
		{
			all.append(title);
		}
		if( summary != null)
		{
			all.append(" ");
			all.append(summary);
		}
		String keywords = (String)results.get("keywords");
		if( keywords != null)
		{
			all.append(" ");
			all.append(keywords);
		}
		if ( body != null)
		{
			all.append(" ");
			all.append(body);
		}
			
		if( all.length() > 0)
		{
			doc.add(new Field("description", all.toString(), Field.Store.YES, Field.Index.TOKENIZED));
		}
		inWriter.addDocument(doc);
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
					index(href,inStatus, inWriter);
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

	public HitTracker getAllHits(WebPageRequest inReq) 
	{
		//TODO: What to do with this?
		return new LuceneHitTracker();
	}

	public String getIndexPath()
	{
		if( fieldIndexPath == null)
		{
			fieldIndexPath = "/" + getCatalogId() + "/search/index";
		}
		return fieldIndexPath;
	}
}
