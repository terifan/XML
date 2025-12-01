package org.terifan.xml;

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;



public class XmlProcessingInstruction extends XmlNode
{
	public XmlProcessingInstruction(Node aNode)
	{
		super(aNode);

		if (!(aNode instanceof ProcessingInstruction))
		{
			throw new IllegalArgumentException("Provided node is not an instance of org.w3c.dom.ProcessingInstruction.");
		}
	}
}
