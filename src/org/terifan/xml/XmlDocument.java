package org.terifan.xml;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static org.terifan.xml.XmlNode.newTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XmlDocument extends XmlNode
{
    public XmlDocument()
    {
		super(newDocument());
    }


    public XmlDocument(byte[] aXmlDocument)
    {
		this(new String(aXmlDocument, UTF_8));
    }


    public XmlDocument(Document aXmlDocument)
    {
		super(aXmlDocument);
    }


    public XmlDocument(String aXmlContent)
    {
		super(parse(aXmlContent, true));
    }


    public XmlDocument(File aXmlFile)
    {
		super(parse(aXmlFile, true));
    }


    public XmlDocument(Reader aXmlStream)
    {
		super(parse(aXmlStream, true));
    }


    public XmlDocument(InputStream aXmlStream)
    {
		super(parse(aXmlStream, true));
    }


    public XmlDocument(Element aXmlElement)
    {
		super(parse(aXmlElement, true));
    }


    public XmlDocument(URL aXmlURL)
    {
		super(parse(aXmlURL, true));
    }


    public XmlDocument(String aXmlContent, boolean aNamespaceAware)
    {
		super(parse(aXmlContent, aNamespaceAware));
    }


    public XmlDocument(File aXmlFile, boolean aNamespaceAware)
    {
		super(parse(aXmlFile, aNamespaceAware));
    }


    public XmlDocument(Reader aXmlStream, boolean aNamespaceAware)
    {
		super(parse(aXmlStream, aNamespaceAware));
    }


    public XmlDocument(InputStream aXmlStream, boolean aNamespaceAware)
    {
		super(parse(aXmlStream, aNamespaceAware));
    }


    public XmlDocument(Element aXmlElement, boolean aNamespaceAware)
    {
		super(parse(aXmlElement, aNamespaceAware));
    }


    public XmlDocument(URL aXmlURL, boolean aNamespaceAware)
    {
		super(parse(aXmlURL, aNamespaceAware));
    }


    public static Document parse(final Object aSource, boolean aNamespaceAware)
    {
		if (aSource == null)
		{
			throw new XmlException("Provided argument is null.");
		}
        try
        {
			try
			{
				if (aSource instanceof String)
				{
					return newBuilder(aNamespaceAware).parse(new InputSource(new StringReader((String)aSource)));
				}
				if (aSource instanceof File)
				{
					return newBuilder(aNamespaceAware).parse((File)aSource);
				}
				if (aSource instanceof Reader)
				{
					return newBuilder(aNamespaceAware).parse(new InputSource((Reader)aSource));
				}
				if (aSource instanceof InputStream)
				{
					return newBuilder(aNamespaceAware).parse((InputStream)aSource);
				}
				if (aSource instanceof URL)
				{
					return newBuilder(aNamespaceAware).parse(aSource.toString());
				}
				throw new IllegalArgumentException("Unsupported type: " + aSource);
			}
			finally
			{
				if (aSource instanceof Closeable)
				{
					((Closeable)aSource).close();
				}
			}
        }
        catch (ParserConfigurationException | SAXException | IOException e)
        {
            throw new XmlException(e);
        }
    }


    public XmlDocument transform(XmlDocument aTemplate)
    {
		try
		{
			XmlDocument result = new XmlDocument();
			newTransformer(aTemplate, true).transform(new DOMSource(mNode), new DOMResult(result.getInternalNode()));
			return result;
		}
		catch (TransformerException e)
		{
            throw new XmlException(e);
		}
    }


    public void transform(XmlDocument aTemplate, OutputStream aOutput) throws IOException
    {
		try
		{
			newTransformer(aTemplate, true).transform(new DOMSource(mNode), new StreamResult(aOutput));
		}
		catch (TransformerException e)
		{
            throw new XmlException(e);
		}
    }


    public void transform(XmlDocument aTemplate, Writer aOutput) throws IOException
    {
		try
		{
			newTransformer(aTemplate, true).transform(new DOMSource(mNode), new StreamResult(aOutput));
		}
		catch (TransformerException e)
		{
            throw new XmlException(e);
		}
    }


    public void transform(XmlDocument aTemplate, File aOutput) throws IOException
    {
		try
		{
			newTransformer(aTemplate, true).transform(new DOMSource(mNode), new StreamResult(aOutput));
		}
		catch (TransformerException e)
		{
            throw new XmlException(e);
		}
    }


	private static DocumentBuilder newBuilder(boolean aNamespaceAware) throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(aNamespaceAware);

		DocumentBuilder documentBuilder = factory.newDocumentBuilder();

		return documentBuilder;
	}


	private static Document newDocument()
	{
        try
        {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			DocumentBuilder documentBuilder = factory.newDocumentBuilder();

            return documentBuilder.newDocument();
        }
        catch (ParserConfigurationException e)
        {
            throw new XmlException(e);
        }
	}


	public XmlElement createElement(String aName)
	{
		return new XmlElement(((Document)mNode).createElement(aName));
	}


	public XmlProcessingInstruction getProcessingInstruction(String aName, String aData)
	{
		Document doc = (Document)mNode;
		ProcessingInstruction pi = doc.createProcessingInstruction(aName, aData);
		doc.insertBefore(pi, doc.getDocumentElement());
		return new XmlProcessingInstruction(pi);
	}


	public XmlElement getFirstElement()
	{
		NodeList nodeList = mNode.getChildNodes();

		if (nodeList != null)
		{
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				Node node = nodeList.item(i);
				if (node instanceof Element)
				{
					return new XmlElement(node);
				}
			}
		}

		return null;
	}
}