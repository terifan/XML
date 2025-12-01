package org.terifan.xml;

import java.util.ArrayList;
import org.w3c.dom.NodeList;


public class XmlNodeList extends ArrayList<XmlNode>
{
	private static final long serialVersionUID = 1L;


	protected XmlNodeList()
	{
	}


	public XmlNodeList(NodeList aNodeList)
	{
		for (int i = 0, sz = aNodeList.getLength(); i < sz; i++)
		{
			super.add(new XmlNode(aNodeList.item(i)));
		}
	}


	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(1024);
		sb.append("XmlNodeList{");
		for (int i = 0, sz = size(); i < sz; i++)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			sb.append(super.get(i).getName());
		}
		sb.append("}");
		return sb.toString();
	}
}