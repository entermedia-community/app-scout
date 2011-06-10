package org.openedit.sitesearch;

import com.openedit.OpenEditException;

public interface Parser {

	 /** Creates the parse for some content. */
	  Parse getParse(Content c) throws OpenEditException;

}
