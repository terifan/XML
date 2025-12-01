package org.terifan.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


public class XPath
{
    private XPathExpression mExpression;


    public XPath(String aExpression)
    {
		this(null, aExpression);
    }


    public XPath(NamespaceContext aNamespaceContext, String aExpression)
    {
        try
        {
			javax.xml.xpath.XPath xpath = XPathFactory.newInstance().newXPath();
			if (aNamespaceContext != null)
			{
				xpath.setNamespaceContext(aNamespaceContext);
			}
            mExpression = xpath.compile(aExpression);
        }
        catch (XPathExpressionException e)
        {
            throw new XmlException(e);
        }
    }


    public XPathExpression getDOMExpression()
    {
        return mExpression;
    }
}