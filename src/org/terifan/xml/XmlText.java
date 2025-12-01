package org.terifan.xml;

import org.w3c.dom.Node;


public class XmlText extends XmlNode
{
	public XmlText(Node aNode)
	{
		super(aNode);
	}


	public String getText()
	{
		return mNode.getTextContent();
	}
}
