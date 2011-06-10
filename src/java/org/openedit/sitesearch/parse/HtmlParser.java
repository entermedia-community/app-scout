package org.openedit.sitesearch.parse;

import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.links.Link;
import org.openedit.sitesearch.Content;
import org.openedit.sitesearch.Parse;
import org.openedit.sitesearch.Parser;

import au.id.jericho.lib.html.CharacterReference;
import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Segment;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;

import com.openedit.OpenEditException;
import com.openedit.util.PathUtilities;

public class HtmlParser implements Parser
{
	private static final Log log = LogFactory.getLog(HtmlParser.class);
	
	public Parse getParse(Content inUrl) throws OpenEditException
	{
		log.info("parse " + inUrl.getUrl());
		Parse results = new Parse();
		Source source = null;
		try
		{
			source= new Source(new URL(inUrl.getUrl().getPath()));
		}
		catch ( Exception ex)
		{
			throw new OpenEditException(ex);
		}
		
		
		Writer l = new StringWriter();
		
		source.setLogWriter(l); // send log messages to stderr
	
		source.fullSequentialParse();
	
		//System.out.println("Document title:");
		String title=getTitle(source);
		results.setTitle(title);
		//System.out.println(title==null ? "(none)" : title);

		//System.out.println("\nDocument keywords:");
		String keywords=getMetaValue(source,"keywords");
		//System.out.println(keywords==null ? "(none)" : keywords);
		if( keywords != null)
		{
			keywords = keywords.replace(","," ");
		}
		//TODO: Each time someone links to this page maybe we should add to a list of links
		if( inUrl.getUrl().getText() != null)
		{
			if( keywords == null)
			{
				keywords = inUrl.getUrl().getText();
			}
			else
			{
				keywords = inUrl.getUrl().getText() + " " + keywords;
			}
		}
		results.put( "keywords", keywords);

		//System.out.println("\nDocument description:");
		String description=getMetaValue(source,"description");
		//System.out.println(description==null ? "(none)" : description);
		results.put( "summary", description);
	
		//System.out.println("\nLinks to other documents:");
		List linkElements=source.findAllElements(HTMLElementName.A);
		
		List links =  new ArrayList();
		String hostName = inUrl.getUrl().getPath();
		hostName = hostName.substring(0, hostName.indexOf("/",8));
		
		for (Iterator i=linkElements.iterator(); i.hasNext();) {
			Element linkElement=(Element)i.next();
			String href=linkElement.getAttributeValue("href");
			if (href==null) continue;
			
			String follow = linkElement.getAttributeValue("rel");
			if( follow != null && "nofollow".equalsIgnoreCase(follow))
			{
				continue;
			}
			//System.out.println(href+" ("+label+")");
			if( href.length() < 2 || href.startsWith("#") || href.indexOf("?") > -1)
			{
				continue;
			}
			int pound = href.indexOf("#");
			if ( pound > -1)
			{
				href = href.substring(0,pound);
			}
			if( href.length() < 2)
			{
				continue;
			}
			// A element can contain other tags so need to extract the text from it:
			Link thelink = new Link();
			String label=linkElement.getContent().extractText();
			thelink.setText(label);
			if( href.startsWith("http") )
			{
				thelink.setPath(href);
			}
			else if( href.startsWith("/"))
			{
				thelink.setPath(hostName + href);
			}
			else
			{
				String cleanhref = PathUtilities.resolveRelativePath(href,inUrl.toString());
				thelink.setPath(hostName + cleanhref);
			}
			links.add(thelink);
			
		} 
		results.put( "links", links);
	
		//System.out.println("\nAll text from BODY (exluding content inside SCRIPT and STYLE elements):");
		Element bodyElement=source.findNextElement(0,HTMLElementName.BODY);
		Segment contentSegment=(bodyElement==null) ? source : bodyElement.getContent();
		results.setText( contentSegment.extractText(true));
		return results;
	}

	private static String getTitle(Source source) {
		Element titleElement=source.findNextElement(0,HTMLElementName.TITLE);
		if (titleElement==null) return null;
		// TITLE element never contains other tags so just decode it collapsing whitespace:
		return CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
	}
	
	private static String getMetaValue(Source source, String key) {
		for (int pos=0; pos<source.length();) {
			StartTag startTag=source.findNextStartTag(pos,"name",key,false);
			if (startTag==null) return null;
			if (startTag.getName()==HTMLElementName.META)
				return startTag.getAttributeValue("content"); // Attribute values are automatically decoded
			pos=startTag.getEnd();
		}
		return null;
	}
	
}
