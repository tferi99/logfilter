package com.ftoth.exam.logfilter;

import com.ftoth.exam.logfilter.config.Config;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class LogFilter
{
    private static Logger log = Logger.getLogger(LogFilter.class);

    private static String inDir;
    private static String outDir;
    private static String cfg;

    public static void main(String[] args)
    {
        try {
            getParams(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        LogFilter app = new LogFilter();
        try {
            app.process(new Config(inDir, outDir, cfg));
        }
        catch (Exception e) {
            log.error("Error during processing", e);
        }
    }

    private static void getParams(String[] args)
    {
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: LogFilter <input directory> <output directory> <filter config properties>");
        }
        inDir = args[0];
        outDir = args[1];
        cfg = args[2];

        File f;
        f = new File(inDir);
        if (!f.isDirectory()) {
            throw new IllegalArgumentException(outDir + " : input directory not found or not a directory");
        }

        f = new File(outDir);
        if (!f.isDirectory()) {
            throw new IllegalArgumentException(outDir + " : output directory not found or not a directory");
        }

        f = new File(cfg);
        if (!f.isFile()) {
            throw new IllegalArgumentException(cfg + " : config file not found or not a file");
        }
    }

    private void process(Config config) throws IOException
    {
        FilterThreadManager m = new FilterThreadManager(config);
        m.execute();
    }
}
