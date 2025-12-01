package org.terifan.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XmlElement extends XmlNode implements Iterable<XmlElement>
{
	public XmlElement(Node aNode)
	{
		super(aNode);

		if (!(aNode instanceof Element))
		{
			throw new IllegalArgumentException("Provided node is not an instance of org.w3c.dom.Element.");
		}
	}


	public XmlElement(XmlNode aNode)
	{
		super(aNode.mNode);

		if (!(aNode.mNode instanceof Element))
		{
			throw new IllegalArgumentException("Provided node is not an instance of org.w3c.dom.Element.");
		}
	}


	public void appendEntity(XmlPropertyProvider aEntity)
	{
		((XmlPropertyProvider)aEntity).appendProperties(this);
	}


	private void setValue(Field field, String name, Object aProvider) throws IllegalStateException
	{
		try
		{
			field.setAccessible(true);
			if (name.startsWith("@"))
			{
				setAttribute(name.substring(1), field.get(aProvider).toString());
			}
			else
			{
				appendTextNode(name, field.get(aProvider).toString());
			}
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Problem getting property " + name + " via field " + field, e);
		}
	}


	private void setValue(Method method, String name, Object aProvider) throws IllegalStateException
	{
		try
		{
			method.setAccessible(true);
			if (name.startsWith("@"))
			{
				setAttribute(name.substring(1), method.invoke(aProvider).toString());
			}
			else
			{
				appendTextNode(name, method.invoke(aProvider).toString());
			}
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Problem getting property " + name + " via method " + method, e);
		}
	}


	@Override
	public XmlElement toElement()
	{
		return (XmlElement)mNode;
	}


	public String getAttribute(String aName)
	{
		return ((Element)mNode).getAttribute(aName);
	}


	public boolean getAttribute(String aName, boolean aDefaultValue)
	{
		String attribute = getAttribute(aName);
		return attribute == null ? aDefaultValue : Boolean.parseBoolean(attribute);
	}


	public int getAttribute(String aName, int aDefaultValue)
	{
		String attribute = getAttribute(aName);
		return attribute == null ? aDefaultValue : Integer.parseInt(attribute);
	}


	public double getAttribute(String aName, double aDefaultValue)
	{
		String attribute = getAttribute(aName);
		return attribute == null ? aDefaultValue : Double.parseDouble(attribute);
	}


	public XmlElement setAttribute(String aName, String aValue)
	{
		((Element)mNode).setAttribute(aName, aValue);
		return this;
	}


	public XmlElement setAttribute(String aName, boolean aValue)
	{
		((Element)mNode).setAttribute(aName, Boolean.toString(aValue));
		return this;
	}


	public XmlElement setAttribute(String aName, int aValue)
	{
		((Element)mNode).setAttribute(aName, Integer.toString(aValue));
		return this;
	}


	public XmlElement setAttribute(String aName, long aValue)
	{
		((Element)mNode).setAttribute(aName, Long.toString(aValue));
		return this;
	}


	@Override
	public Iterator<XmlElement> iterator()
	{
		final NodeList list = getInternalNode().getChildNodes();
		return new Iterator<XmlElement>()
		{
			int index;
			XmlElement next;

			@Override
			public boolean hasNext()
			{
				if (next == null)
				{
					while (index < list.getLength())
					{
						Node node = list.item(index++);
						if (node instanceof Element)
						{
							next = new XmlElement(node);
							return true;
						}
					}
				}
				return next != null;
			}

			@Override
			public XmlElement next()
			{
				XmlElement tmp = next;
				next = null;
				return tmp;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}


	public void importXml(XmlElement aElement)
	{
		appendChild(new XmlElement(mNode.getOwnerDocument().importNode(aElement.mNode, true)));
	}


	public XmlElement setText(String aText)
	{
		mNode.setTextContent(aText);
		return this;
	}
}
