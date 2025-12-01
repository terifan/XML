package org.terifan.xml;

import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.Arrays;


@Deprecated
public class XmlReader
{
	private String mName;
	private String mValue;
	private Readable mReadable;
	private PushbackReader mReader;
	private boolean mEOF;
	private boolean mInsideElement;


	public XmlReader(Readable aReadable) throws IOException
	{
		mReadable = aReadable;
		mReader = new PushbackReader(new Reader()
		{
			public void close() throws IOException
			{
			}
			public int read(char [] cbuf, int off, int len) throws IOException
			{
				CharBuffer buffer = CharBuffer.wrap(cbuf, off, len);
				return mReadable.read(buffer);
			}
		});
	}


//	public boolean hasAttributes()
//	public boolean hasValue()
//	public boolean isEmptyElement() // (for example, <MyElement/>).
//	public String getNodeType()


	public String getValue()
	{
		return mValue;
	}


	public String getName()
	{
		return mName;
	}


	public void moveToElement(String aName) throws IOException
	{
		do
		{
			if (getName().equals(aName))
			{
				return;
			}
		}
		while (moveToNextElement());
		throw new IOException("Element not found: name: " + aName);
	}


	public boolean moveToNextElement() throws IOException
	{
		for (;;)
		{
			findChar('<');
			read();

			int c = peek();

			if (mEOF) return false;

			if (c == '!') // comment
			{
				findPattern("--".toCharArray());
			}
			else if (c == '[') // cdata
			{
				findPattern("]]>".toCharArray());
			}
			else if (c == '?') // processing instruction
			{
				findPattern("?>".toCharArray());
			}
			else if (c == '/') // end of element
			{
				findChar('>');
			}
			else
			{
				mName = new String(readName());
				mValue = null;

				mInsideElement = Character.isWhitespace(peek());

				return true;
			}
		}
	}


	public String readAttributeValue(String aName) throws IOException
	{
		while (moveToNextAttribute())
		{
			if (getName().equals(aName))
			{
				return getValue();
			}
		}
		throw new IOException("Attribute not found: " + aName);
	}


	public boolean moveToNextAttribute() throws IOException
	{
		trim();

		int c = peek();
		if (mEOF || c == '/' || c == '>')
		{
			return false;
		}

		mName = new String(readName());

		findChar('\"', '\'');
		c = read();

		mValue = readValue((char)c);

		return true;
	}


	public String readElementString() throws IOException
	{
		if (mInsideElement)
		{
			while (moveToNextAttribute()) {}
			read();
			mInsideElement = false;
		}

		if (peek() == '/')
		{
			read();
		}
		if (peek() == '>')
		{
			read();
		}

		return readValue('<');
	}


	public char [] readName() throws IOException
	{
		CharArrayWriter buffer = new CharArrayWriter();

		int b = read();

		if (b != '_' && b != ':' && !Character.isLetterOrDigit(b))
		{
			throw new IllegalArgumentException("Invalid element or attribute name: " + (char)b);
		}

		buffer.write(b);

		for (;;)
		{
			b = peek();

			if (b == -1)
			{
				mEOF = true;
				break;
			}
			if (Character.isWhitespace(b) || b == '>' || b == '/' || b == '=')
			{
				break;
			}
			if (b != '-' && b != '_' && b != '.' && b != ':' && !Character.isLetterOrDigit(b))
			{
				throw new IllegalArgumentException("Invalid element or attribute name: " + (char)b);
			}

			buffer.write(read());
		}

		return buffer.toCharArray();
	}


	private String readValue(char aBreakChar) throws IOException
	{
		CharArrayWriter buffer = new CharArrayWriter();

		for (;;)
		{
			int b = read();

			if (b == -1)
			{
				mEOF = true;
				break;
			}
			if (b == aBreakChar)
			{
				break;
			}

			buffer.write(b);
		}

		return new String(buffer.toCharArray());
	}


	public void close() throws IOException
	{
		mReader.close();

		if (mReadable instanceof Closeable)
		{
			((Closeable)mReadable).close();
		}
	}


	private void trim() throws IOException
	{
		for (;;)
		{
			int b = peek();
			if (b == -1)
			{
				mEOF = true;
				return;
			}
			if (b != ' ')
			{
				return;
			}
			read();
		}
	}


/*	private void findNextName() throws IOException
	{
		for (;;)
		{
			int b = peek();

			if (b == -1)
			{
				mEOF = true;
				return;
			}
			if (b != '_' && b != ':' && !Character.isLetterOrDigit(b))
			{
				return;
			}

			read();
		}
	}*/


	private char [] findChar(char ... aCharacters) throws IOException
	{
		CharArrayWriter buffer = new CharArrayWriter();

		outer: for (;;)
		{
			int b = peek();

			if (b == -1)
			{
				mEOF = true;
				break;
			}

			for (int i = 0; i < aCharacters.length; i++)
			{
				if (b == aCharacters[i])
				{
					break outer;
				}
			}

			buffer.write(read());
		}

		return buffer.toCharArray();
	}


	private void findPattern(char [] aPattern) throws IOException
	{
		char [] match = new char[aPattern.length];

		for (;;)
		{
			int b = read();

			if (b == -1)
			{
				mEOF = true;
				return;
			}

			match[match.length-1] = (char)b;

			if (Arrays.equals(match, aPattern))
			{
				return;
			}

			System.arraycopy(match, 1, match, 0, match.length-1);
		}
	}


	private int read() throws IOException
	{
		return mReader.read();
	}


	private int peek() throws IOException
	{
		int c = mReader.read();
		mReader.unread(c);
		return c;
	}
}