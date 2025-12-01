package org.terifan.xml;


@FunctionalInterface
public interface XmlNodeVisitor
{
	default public boolean match(XmlNode aNode)
	{
		return true;
	}

	default public Object entering(XmlNode aNode)
	{
		return null;
	}

	default public Object leaving(XmlNode aNode)
	{
		return null;
	}

	default public Object attribute(XmlNode aNode, String aName, String aValue)
	{
		return null;
	}

	public Object process(XmlNode aNode);
}
