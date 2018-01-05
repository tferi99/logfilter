package com.ftoth.exam.logfilter.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config
{
    //------------------------------ properties ----------------------------------
    private String inDir;
    private String outDir;
    private Properties filters;

    public String getInDir()
    {
        return inDir;
    }

    public String getOutDir()
    {
        return outDir;
    }

    public Properties getFilters()
    {
        return filters;
    }

    //------------------------------ startup ----------------------------------
    public Config(String inDir, String outDir, String configFile)
    {
        this.inDir = inDir;
        this.outDir = outDir;

        filters = new Filters(configFile);
    }


    public static class Filters extends Properties
   {
        public Filters(String configFile)
        {
            InputStream in = null;
            try {
                in = new FileInputStream(configFile);
                load(in);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
