importPackage( Packages.com.openedit.util );
importPackage( Packages.java.util );
importPackage( Packages.java.lang );
importPackage( Packages.java.io );
importPackage( Packages.com.openedit.modules.update );
importPackage( Packages.com.openedit.modules.scheduler );

var war = "http://dev.entermediasoftware.com/jenkins/job/app-scout/lastSuccessfulBuild/artifact/deploy/app-scout/ROOT.war";

var root = moduleManager.getBean("root").getAbsolutePath();
var web = root + "/WEB-INF";
var tmp = web + "/tmp";

log.add("1. GET THE LATEST WAR FILE");
var downloader = new Downloader();
downloader.download( war, tmp + "/ROOT.war");

log.add("2. UNZIP WAR FILE");
var unziper = new ZipUtil();
unziper.unzip(  tmp + "/ROOT.war",  tmp );

log.add("3. REPLACE LIBS");
var files = new FileUtils();
files.deleteMatch( web + "/lib/openedit-search*.jar");
files.deleteMatch( web + "/lib/nutch*.jar");
files.deleteMatch( web + "/lib/jericho-html-*.jar");
files.deleteMatch( web + "/lib/jakarta-oro*.jar");
files.deleteMatch( web + "/lib/PDFBox*.jar");
files.deleteMatch( web + "/lib/FontBox*.jar");
files.copyFileByMatch( tmp + "/WEB-INF/lib/openedit-search*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/jericho-html*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/PDFBox*.jar", web + "/lib/");
files.copyFileByMatch( tmp + "/WEB-INF/lib/FontBox*.jar", web + "/lib/");

log.add("4. UPGRADE BASE DIR");
files.deleteAll( root + "/base/search");
files.deleteAll( root + "/WEB-INF/base/search");
files.copyFiles( tmp + "/WEB-INF/base/search", root + "/WEB-INF/base/search");

var settings = new File( root, "/search/_site.xconf" );
if( !settings.exists() )
{
	log.add("5. SETUP /search/ dir");

	files.copyFiles( tmp + "/WEB-INF/base/search/admin/starter/", root + "/search/");
	//Edit the search.xml file
 	var replace = new File(root + "/search/search.xml");
	var url = context.getPageValue("url_util");
	files.replace(replace,"yourdomain",  url.siteRoot() ); 
	
	//Add the scheduler entry
	var action = new Action();
	action.setPath("/search/admin/crawl.html");
	action.setUserName(context.getUserName());
	action.setDelay("1h");
	action.setPeriod("24h");
	var scheduler = moduleManager.getBean("Scheduler");
	scheduler.getScheduler().addAction(action);	
}

log.add("5. CLEAN UP");
files.deleteAll(tmp);

log.add("6. UGRADE COMPLETED");