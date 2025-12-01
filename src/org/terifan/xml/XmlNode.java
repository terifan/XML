package org.terifan.xml;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;


public class XmlNode
{
	protected Node mNode;


	public XmlNode(Node aNode)
	{
		if (aNode == null)
		{
			throw new IllegalArgumentException("Provided node is null.");
		}

		mNode = aNode;
	}


	public XmlNode getParent()
	{
		if (mNode.getParentNode() == null)
		{
			if (mNode.getOwnerDocument() == null)
			{
				return null;
			}
			else
			{
				return new XmlDocument(mNode.getOwnerDocument());
			}
		}
		else
		{
			return new XmlNode(mNode.getParentNode());
		}
	}


    public XmlElement getElement(XPath aXPath)
    {
		XmlNode node = getNode(aXPath);
		if (node == null)
		{
			return null;
		}
		if (node instanceof XmlElement)
		{
			return (XmlElement)node;
		}
		return new XmlElement(node);
	}


    public XmlNode getNode(XPath aXPath)
    {
        try
        {
			Node node = (Node)aXPath.getDOMExpression().evaluate(mNode, XPathConstants.NODE);
			if (node == null)
			{
				return null;
			}
            return new XmlNode(node);
        }
        catch (XPathExpressionException e)
        {
            throw new XmlException(e);
        }
    }


    public XmlNodeList getList(XPath aXPath)
    {
        try
        {
			NodeList nodeList = (NodeList)aXPath.getDOMExpression().evaluate(mNode, XPathConstants.NODESET);
			if (nodeList == null)
			{
				return new XmlNodeList();
			}
            return new XmlNodeList(nodeList);
        }
        catch (XPathExpressionException e)
        {
            throw new XmlException(e);
        }
    }


	/**
	 * Return an array of elements matching the XPath. This method will always
	 * return an array.
	 */
    public XmlElement [] getElements(XPath aXPath)
    {
        try
        {
			NodeList nodeList = (NodeList)aXPath.getDOMExpression().evaluate(mNode, XPathConstants.NODESET);
			if (nodeList == null)
			{
				return new XmlElement[0];
			}
			XmlElement [] elements = new XmlElement[nodeList.getLength()];
			int size = 0;
			for (int i = 0; i < elements.length; i++)
			{
				Node node = nodeList.item(i);
				if (node instanceof Element)
				{
					elements[size++] = new XmlElement(node);
				}
			}
            return Arrays.copyOfRange(elements, 0, size);
        }
        catch (XPathExpressionException e)
        {
            throw new XmlException(e);
        }
    }


	/**
	 * Return an array of elements matching the XPath. This method will always
	 * return an array.
	 */
    public XmlElement [] getElements(String aPath)
    {
		return getElements(new XPath(aPath));
    }


    public XmlElement [] getChildElements()
    {
		NodeList nodeList = mNode.getChildNodes();
		if (nodeList == null)
		{
			return new XmlElement[0];
		}
		XmlElement [] elements = new XmlElement[nodeList.getLength()];
		int size = 0;
		for (int i = 0; i < elements.length; i++)
		{
			Node node = nodeList.item(i);
			if (node instanceof Element)
			{
				elements[size++] = new XmlElement(node);
			}
		}
		return Arrays.copyOfRange(elements, 0, size);
    }


    public XmlNode [] getChildNodes()
    {
		NodeList nodeList = mNode.getChildNodes();
		if (nodeList == null)
		{
			return new XmlElement[0];
		}
		XmlNode [] nodes = new XmlNode[nodeList.getLength()];
		for (int i = 0; i < nodes.length; i++)
		{
			Node node = nodeList.item(i);
			if (node instanceof Element)
			{
				nodes[i] = new XmlElement(node);
			}
			else if (node instanceof ProcessingInstruction)
			{
				nodes[i] = new XmlProcessingInstruction(node);
			}
			else
			{
				nodes[i] = new XmlNode(node);
			}
		}
		return nodes;
    }


    public String getText(XPath aXPath, String aDefaultValue)
    {
		String s = getText(aXPath);
		if (s == null)
		{
			return aDefaultValue;
		}
		return s;
	}


    public String getText(XPath aXPath, Supplier<String> aDefaultValue)
    {
		String s = getText(aXPath);
		if (s == null)
		{
			return aDefaultValue.get();
		}
		return s;
	}


    public String getText(XPath aXPath)
    {
        try
        {
            return (String)aXPath.getDOMExpression().evaluate(mNode, XPathConstants.STRING);
        }
        catch (XPathExpressionException e)
        {
            throw new XmlException(e);
        }
    }


    public ArrayList<String> getTextArray(XPath aXPath)
    {
		return getTextArray(aXPath, true);
	}


	public ArrayList<String> getTextArray(XPath aXPath, boolean aIncludeEmptyElements)
    {
        try
        {
            XmlNodeList list = new XmlNodeList((NodeList)aXPath.getDOMExpression().evaluate(mNode, XPathConstants.NODESET));

			ArrayList<String> output = new ArrayList<>();

			list.forEach(node ->
			{
				String text = node.getValue();
				if (aIncludeEmptyElements || !text.isEmpty())
				{
					output.add(text);
				}
			});

			return output;
        }
        catch (XPathExpressionException e)
        {
            throw new XmlException(e);
        }
	}


    public XmlElement getElement(String aPath)
    {
		XmlNode node = getNode(aPath);
		if (node == null)
		{
			return null;
		}
		return new XmlElement(node);
	}


	/**
	 * Get or create an element using an attribute as grouping key.
	 *
	 * e.g. element.getOrCreateElement("file-list", "id", file.getParentFileId()).appendElement("item").appendEntity(file, false);
	 *
	 * @param aName
	 *   name of the element
	 * @param aAttribute
	 *   attribute used to identify existing nodes
	 * @param aValue
	 *   the expected value of the attribute
	 * @return
	 *   the existing or created element
	 */
    public XmlElement getOrCreateElementGroup(String aName, String aAttribute, Object aValue)
    {
		XmlNode node = getNode(new XPath(aName + "[@"+aAttribute+"='"+aValue+"']"));
		if (node == null)
		{
			return appendElement(aName).setAttribute(aAttribute, aValue.toString());
		}
		return new XmlElement(node);
	}


    public XmlNode getNode(String aPath)
    {
		assertNodePath(aPath);

		if (aPath.startsWith("/"))
		{
			return new XmlNode(getOwner()).getNode(aPath.substring(1));
		}

		Node node = mNode;
		String [] paths = aPath.split("/");
		for (int j = 0; j < paths.length; j++)
		{
			String path = paths[j];
			boolean last = paths.length-1 == j;
			boolean found = false;
			NodeList list = node.getChildNodes();
			for (int i = 0, sz = list.getLength(); i < sz; i++)
			{
				if (path.startsWith("@"))
				{
					if (!last)
					{
						throw new XmlException("Attributes must be the last path element: path: " + aPath+", element: "+path);
					}
					return new XmlNode(((Element)node).getAttributeNode(path.substring(1)));
				}
				Node child = list.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && getNodeName(child).equals(path))
				{
					if (last)
					{
						return new XmlNode(child);
					}
					node = child;
					found = true;
					break;
				}
			}
			if (!found)
			{
				return null;
			}
		}
		return null;
    }


    public String getText(String aPath, String aDefaultValue)
    {
		String s = getText(aPath);
		if (s == null)
		{
			return aDefaultValue;
		}
		return s;
	}


    public String getText(String aPath, Supplier<String> aDefaultValue)
    {
		String s = getText(aPath);
		if (s == null)
		{
			return aDefaultValue.get();
		}
		return s;
	}


    public String getText(String aPath)
    {
		assertNodePath(aPath);

		XmlNode node = getNode(aPath);
		if (node == null)
		{
			return null;
		}

		return node.getValue();
    }


    public XmlNodeList getList(String aPath)
    {
		assertNodePath(aPath);

		if (aPath.startsWith("/"))
		{
			return new XmlNode(getOwner()).getList(aPath.substring(1));
		}

		XmlNodeList list = new XmlNodeList();

		getList(aPath, list);

		return list;
	}



    public String [] getTextArray(String aPath)
    {
		assertNodePath(aPath);

		if (aPath.startsWith("/"))
		{
			return new XmlNode(getOwner()).getTextArray(aPath.substring(1));
		}

		XmlNodeList list = new XmlNodeList();

		getList(aPath, list);

		String [] text = new String[list.size()];

		for (int i = 0; i < list.size(); i++)
		{
			text[i] = list.get(i).getValue();
		}

		return text;
	}


    private void getList(String aPath, XmlNodeList aList)
    {
		int index = aPath.indexOf('/');
		boolean last = index == -1;
		String path = last ? aPath : aPath.substring(0, index);
		String remaining = aPath.substring(index+1);

		if (path.startsWith("@"))
		{
			if (!last)
			{
				throw new XmlException("Attributes must be the last path element: path: " + aPath+", element: "+path);
			}
			aList.add(new XmlNode(((Element)mNode).getAttributeNode(path.substring(1))));
			return;
		}

		NodeList list = mNode.getChildNodes();
		for (int i = 0, sz = list.getLength(); i < sz; i++)
		{
			XmlNode child = new XmlNode(list.item(i));
			if (child.getName().equals(path))
			{
				if (last)
				{
					aList.add(child);
				}
				else
				{
					child.getList(remaining, aList);
				}
			}
		}
    }


	private void assertNodePath(String aPath)
	{
		boolean fail = false;
		boolean atFound = false;
		char d = 0;
		for (int i = 0, sz = aPath.length(); !fail && i < sz; i++)
		{
			char c = aPath.charAt(i);
			if (c == '[' || c == ']' || c == '\'' || c == '\"' || c == '*' || c == '=' || (c == '/' && d == '/'))
			{
				fail = true;
			}
			if (atFound && c == '/')
			{
				fail = true;
			}
			if (c == '@')
			{
				if (atFound)
				{
					fail = true;
				}
				atFound = true;
			}
			d = c;
		}
		if (fail)
		{
			throw new IllegalArgumentException("aPath must be a literal node name path. (You may want to use an XPath query?): path: " + aPath);
		}
	}


	public String getName()
	{
		return getNodeName(mNode);
	}


	public String getValue()
	{
		Node c = mNode.getFirstChild();
		if (c != null)
		{
			return c.getNodeValue();
		}
		return mNode.getTextContent();
	}


	public XmlElement toElement()
	{
		return new XmlElement(mNode);
	}


	public String getAttribute(String aName)
	{
		String v = getAttribute(aName, (String)null);
		if (v == null)
		{
			return null;
		}
		return v;
	}


	public boolean hasAttribute(String aName)
	{
		return getAttribute(aName, (String)null) != null;
	}


	public String getAttribute(String aName, String aDefaultValue)
	{
		if (mNode instanceof Element)
		{
			Element el = (Element)mNode;
			if (el.hasAttribute(aName))
			{
				return el.getAttribute(aName);
			}
			return aDefaultValue;
		}
		throw new XmlException("This XmlNode is not an XmlElement or an instance of org.w3c.dom.Element: " + getClass().getName());
	}


	public String getAttribute(String aName, Supplier<String> aDefaultValue)
	{
		if (mNode instanceof Element)
		{
			Element el = (Element)mNode;
			if (el.hasAttribute(aName))
			{
				return el.getAttribute(aName);
			}
			return aDefaultValue.get();
		}
		throw new XmlException("This XmlNode is not an XmlElement or an instance of org.w3c.dom.Element: " + getClass().getName());
	}


	public XmlElement setAttribute(String aName, String aValue)
	{
		if (mNode instanceof Element)
		{
			Element el = (Element)mNode;
			el.setAttribute(aName, aValue);
			if (this instanceof XmlElement)
			{
				return (XmlElement)this;
			}
			return null;
		}
		throw new XmlException("This XmlNode is not an XmlElement or an instance of org.w3c.dom.Element: " + getClass().getName());
	}


	public XmlElement appendElement(String aName)
	{
		XmlElement node = getDocument().createElement(aName);
		mNode.appendChild(node.mNode);
		return node;
	}


	public XmlElement appendElement(String aName, Consumer<XmlElement> aConsumer)
	{
		XmlElement el = appendElement(aName);
		aConsumer.accept(el);
		return el;
	}


	/**
	 * Append a child node and return this XmlNode
	 * @return this XmlNode
	 */
	public <T extends XmlNode> T appendChild(XmlNode aNode)
	{
		if (aNode.getOwner() != getOwner())
		{
			aNode = new XmlElement(mNode.getOwnerDocument().adoptNode(aNode.mNode));
		}

		mNode.appendChild(aNode.mNode);
		return (T)this;
	}


	public XmlDocument getDocument()
	{
		return new XmlDocument(getOwner());
	}


	@Override
	public String toString()
	{
		return "XmlNode{" + (mNode == null ? "null" : mNode.getNodeName()) + "}";
	}


	/**
	 * Append text node and return this XmlNode.
	 *
	 * Note: this method will escape illegal characters!
	 *
	 * @param aText
	 *   if null then nothing happens
	 * @return
	 *   this XmlNode
	 */
	public <T extends XmlNode> T appendTextNode(String aNodeName, Object aText)
	{
		if (aText == null)
		{
			return (T)this;
		}

		Element node = getOwner().createElement(aNodeName);
		setTextContentBackwardComp(node, "" + aText);
		mNode.appendChild(node);
		return (T)this;
	}


	/**
	 * Append text node and return the created text node.
	 *
	 * Note: this method will escape illegal characters!
	 *
	 * @param aText
	 *   if null then nothing happens
	 * @return
	 *   this XmlNode
	 */
	public XmlElement createTextNode(String aNodeName, Object aText)
	{
		if (aText == null)
		{
			return null;
		}

		Element node = getOwner().createElement(aNodeName);
		setTextContentBackwardComp(node, aText.toString());
		mNode.appendChild(node);
		return new XmlElement(node);
	}


	/** for backward compatibility */
	static void setTextContentBackwardComp(Node aNode, String aText)
	{
		NodeList list = aNode.getChildNodes();
		for (int i = list.getLength(); --i >= 0;)
		{
			if (list.item(i).getNodeType() == Node.TEXT_NODE)
			{
				aNode.removeChild(list.item(i));
			}
		}

		aNode.appendChild(aNode.getOwnerDocument().createTextNode(aText));
	}


	public void writeTo(File aFile)
	{
		try
		{
			newTransformer(true).transform(new DOMSource(mNode), new StreamResult(aFile));
		}
		catch (TransformerException e)
		{
			throw new IllegalStateException(e);
		}
	}


	public void writeTo(Writer aWriter)
	{
		try
		{
			newTransformer(true).transform(new DOMSource(mNode), new StreamResult(aWriter));
		}
		catch (TransformerException e)
		{
			throw new IllegalStateException(e);
		}
	}


	public void writeTo(OutputStream aOutputStream)
	{
		try
		{
			newTransformer(true).transform(new DOMSource(mNode), new StreamResult(aOutputStream));
		}
		catch (TransformerException e)
		{
			throw new IllegalStateException(e);
		}
	}


	public String toXmlString()
	{
		return toXmlString(false, false);
	}


	public String toXmlString(boolean aOmitXmlDeclaration, boolean aOmitIndent)
	{
		try
		{
			CharArrayWriter cw = new CharArrayWriter();
			Transformer transformer = newTransformer(aOmitIndent);
			if (aOmitXmlDeclaration)
			{
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}
//			if (aOmitIndent)
//			{
//				transformer.setOutputProperty(OutputKeys.INDENT, "no");
//			}
			transformer.transform(new DOMSource(mNode), new StreamResult(cw));
			return cw.toString().trim();
		}
		catch (TransformerException e)
		{
			throw new IllegalStateException(e);
		}
	}


	/**
	 * Return the document as a UTF-8 encoded byte array.
	 */
	public byte[] toByteArray()
	{
		try
		{
			return toXmlString().getBytes("utf-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException(e);
		}
	}


	public Node getInternalNode()
	{
		return mNode;
	}


	public Object visit(XmlNodeVisitor aVisitor)
	{
		NodeList list = mNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			XmlNode node = new XmlNode(list.item(i));

			if (aVisitor.match(node))
			{
				Object o = aVisitor.entering(node);

				if (o != null)
				{
					return o;
				}

				o = node.visit(aVisitor);

				if (o != null)
				{
					return o;
				}

				o = aVisitor.leaving(node);

				if (o != null)
				{
					return o;
				}
			}
		}

		return null;
	}


	private static String getNodeName(Node aNode)
	{
		String s = aNode.getLocalName();
		if (s == null)
		{
			s = aNode.getNodeName();
		}
		return s;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof XmlNode)
		{
			return mNode.equals(((XmlNode)obj).mNode);
		}
		return false;
	}


	@Override
	public int hashCode()
	{
		return mNode.hashCode();
	}


	private Document getOwner()
	{
		return (Document)((mNode instanceof Document) ? mNode : mNode.getOwnerDocument());
	}


	static Transformer newTransformer(boolean aOmitIndent) throws TransformerConfigurationException, TransformerFactoryConfigurationError
	{
		return newTransformer(null, aOmitIndent);
	}


	public static Transformer newTransformer(XmlDocument aTemplate, boolean aOmitIndent) throws TransformerConfigurationException, TransformerFactoryConfigurationError
	{
		Transformer transformer;
		if (aTemplate == null)
		{
			transformer = TransformerFactory.newInstance().newTransformer();
		}
		else
		{
			transformer = TransformerFactory.newInstance().newTransformer(new DOMSource(aTemplate.getInternalNode()));
		}

		// WARNING! enabling these will break javascript code since non-existing line breaks are added after <br> tags!
		if (aOmitIndent)
		{
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
		}
		else
		{
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
		}

		transformer.setOutputProperty("encoding", "utf-8");
		transformer.setErrorListener(new ErrorListener() {
			@Override
			public void warning(TransformerException aException) throws TransformerException
			{
				throw new IllegalStateException("XSLT warning while transforming document.", aException);
			}
			@Override
			public void error(TransformerException aException) throws TransformerException
			{
				throw new IllegalStateException("XSLT error while transforming document.", aException);
			}
			@Override
			public void fatalError(TransformerException aException) throws TransformerException
			{
				throw new IllegalStateException("XSLT fatal error while transforming document.", aException);
			}
		});
		return transformer;
	}


	/**
	 * Removes this Node from it's parent.
	 */
	public void remove()
	{
		mNode.getParentNode().removeChild(mNode);
	}


//	public static void main(String ... args)
//	{
//		try
//		{
//			XmlDocument d = new XmlDocument();
//
//			d.appendElement("a").appendTextNode("b", "c");
//
//			System.out.println(d.toXmlString());
//
//			d.getElement("a").getElement("b").setText("d");
//
//			System.out.println(d.toXmlString());
//
//			d.getElement("a/b").remove();
//			System.out.println(d.toXmlString());
//
//			d.getElement("a").remove();
//			System.out.println(d.toXmlString());
//		}
//		catch (Throwable e)
//		{
//			e.printStackTrace(System.out);
//		}
//	}
}