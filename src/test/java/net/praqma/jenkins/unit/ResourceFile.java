/*
 * The MIT License
 *
 * Copyright 2013 hugo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins.unit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.junit.rules.ExternalResource;

/**
 *
 * @author hugo
 */
public class ResourceFile extends ExternalResource
{
    String res;
    File file = null;
    InputStream stream;

    public ResourceFile(String res)
    {
        this.res = res;
    }

    public File getFile() throws IOException
    {
        if (file == null)
        {
            createFile();
        }
        return file;
    }

    public InputStream getInputStream()
    {
        return stream;
    }

    public InputStream createInputStream()
    {
        return getClass().getResourceAsStream(res);
    }

    public String getContent() throws IOException
    {
        return getContent("utf-8");
    }

    public String getContent(String charSet) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(createInputStream(),
            Charset.forName(charSet));
        char[] tmp = new char[4096];
        StringBuilder b = new StringBuilder();
        try
        {
            while (true)
            {
                int len = reader.read(tmp);
                if (len < 0)
                {
                    break;
                }
                b.append(tmp, 0, len);
            }
            reader.close();
        }
        finally
        {
            reader.close();
        }
        return b.toString();
    }

    @Override
    protected void before() throws Throwable
    {
        super.before();
        stream = getClass().getResourceAsStream(res);
    }

    @Override
    protected void after()
    {
        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            // ignore
        }
        if (file != null)
        {
            file.delete();
        }
        super.after();
    }

    private void createFile() throws IOException
    {
        file = new File(".",res);
        InputStream stream = getClass().getResourceAsStream(res);
        try
        {
            file.createNewFile();
            FileOutputStream ostream = null;
            try
            {
                ostream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                while (true)
                {
                    int len = stream.read(buffer);
                    if (len < 0)
                    {
                        break;
                    }
                    ostream.write(buffer, 0, len);
                }
            }
            finally
            {
                if (ostream != null)
                {
                    ostream.close();
                }
            }
        }
        finally
        {
            stream.close();
        }
    }

}