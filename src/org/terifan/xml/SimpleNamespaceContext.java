package org.terifan.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;


/**
 * Class used as a lookup table for name space prefixes.
 *
 * note: default name spaces are not supported by Java default XPath (1.0) implementation.
 */
public class SimpleNamespaceContext implements NamespaceContext
{
	private HashMap<String,String> mContexts;


	public SimpleNamespaceContext()
	{
		mContexts = new HashMap<>();
	}


	public SimpleNamespaceContext add(String aName, String aContext)
	{
		mContexts.put(aName, aContext);
		return this;
	}


	@Override
	public String getNamespaceURI(String aPrefix)
	{
		String ctx = mContexts.get(aPrefix);

		if (ctx == null)
		{
			return XMLConstants.NULL_NS_URI;
		}

		return ctx;
	}


	@Override
	public String getPrefix(String aNamespaceURI)
	{
		for (Entry<String,String> entry : mContexts.entrySet())
		{
			if (entry.getValue().equals(aNamespaceURI))
			{
				return entry.getKey();
			}
		}

		return null;
	}


	@Override
	public Iterator getPrefixes(String aNamespaceURI)
	{
		ArrayList<String> result = new ArrayList<>();

		for (Entry<String,String> entry : mContexts.entrySet())
		{
			if (entry.getValue().equals(aNamespaceURI))
			{
				result.add(entry.getKey());
			}
		}

		return result.iterator();
	}
}
