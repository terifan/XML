package org.terifan.xml;


public class XmlException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
    public XmlException()
    {
    }

    public XmlException(Exception aException)
    {
        super(aException);
    }

    public XmlException(String aMessage, Exception aException)
    {
        super(aMessage, aException);
    }

    public XmlException(String aMessage)
    {
        super(aMessage);
    }
}