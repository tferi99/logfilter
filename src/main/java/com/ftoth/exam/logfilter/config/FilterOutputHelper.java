package com.ftoth.exam.logfilter.config;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

public class FilterOutputHelper
{
    //------------------------------ properties ----------------------------------
    private String name;
    private String patternValue;
    private Pattern pattern;
    private Writer out;
    private int found = 0;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPatternValue()
    {
        return patternValue;
    }

    public void setPatternValue(String patternValue)
    {
        this.patternValue = patternValue;
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public Writer getOut()
    {
        return out;
    }

    public int getFound()
    {
        return found;
    }

    //------------------------------ startup ----------------------------------
    public FilterOutputHelper(String name, String patternValue, Writer out)
    {
        this.name = name;
        this.patternValue = patternValue;
        this.out = out;
        pattern = Pattern.compile(patternValue);
    }

    public synchronized void found(String line) throws IOException
    {
        out.write(line + "\n");
        found++;
    }
}
