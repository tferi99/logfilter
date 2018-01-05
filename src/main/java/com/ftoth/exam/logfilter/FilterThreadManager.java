package com.ftoth.exam.logfilter;

import com.ftoth.exam.logfilter.config.Config;
import com.ftoth.exam.logfilter.config.FilterOutputHelper;
import com.ftoth.exam.logfilter.utils.SystemInfo;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilterThreadManager
{
    private static Logger log = Logger.getLogger(FilterThreadManager.class);

    private static final int OUTPUT_BUFFER_SIZE = 1024 * 1024;
    private static String OUT_EXT = ".txt";
    private static final String OUTPUT_CHARSET = "windows-1252";

    private Config config;
    private List<FilterOutputHelper> filters;
    private List<String> inFiles;

    //------------------------------ startup ----------------------------------
    public FilterThreadManager(Config config)
    {
        this.config = config;
    }

    public void execute() throws IOException
    {
        init();

        if (inFiles.isEmpty()) {
            log.warn("There is no input file in [" + config.getInDir() + "]");
            return;
        }

        // start threads in a pool
        int cores = SystemInfo.getCpuCoreCount();
        if (log.isDebugEnabled()) {
            log.debug("Number of processor cores:" +  cores);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(cores);

        // creating thread for processing
        String pathSep = File.separator;
        int threadId = 0;
        CountDownLatch threadCompletionSignalCounter = new CountDownLatch(inFiles.size());
        for (String inFile : inFiles) {
            Runnable thr = new FilterThread(threadId++, config.getInDir() + pathSep + inFile, filters, threadCompletionSignalCounter);
            executorService.submit(thr);
        }
        executorService.shutdown();

        try {
            threadCompletionSignalCounter.await();
        }
        catch (InterruptedException e) {
            log.error("Error in task completion signal counter", e);
        }

        cleanup();
    }

    private void init() throws IOException
    {
        // filters (output locks)
        filters = initFilters();
        if (log.isDebugEnabled()) {
            log.debug("Filters has been initialized: " + filters.size());
        }

        // input files
        inFiles = getFileList(config.getInDir());
        if (log.isDebugEnabled()) {
            log.debug("Input files has been initialized: " + inFiles.size());
        }
    }

    private void cleanup()
    {
        cleanupFilters();

        if (log.isDebugEnabled()) {
            log.debug("-----------------------------------------------");
            log.debug("Filter matching lines:");
            log.debug("-----------------------------------------------");
            for (FilterOutputHelper filter: filters) {
                log.debug("Filter[" + filter.getName() + "]: " + filter.getFound());
            }
            log.debug("-----------------------------------------------");
        }
    }

    //------------------------------ helpers ----------------------------------
    private List<String> getFileList(String dirName)
    {
        List<String> result = new ArrayList<String>();
        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir + " : is not a directory");
        }

        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                result.add(f.getName());
            }
        }
        return result;
    }

    private List<FilterOutputHelper> initFilters() throws IOException
    {
        if (log.isDebugEnabled()) {
            log.debug("Initialization of filters");
        }

        String pathSep = File.separator;
        List<FilterOutputHelper> result = new ArrayList<FilterOutputHelper>();
        for (Object key : config.getFilters().keySet()) {
            String name = (String) key;
            String pattern = (String)config.getFilters().get(key);
            String outFile = config.getOutDir() + pathSep + name + OUT_EXT;
            Writer w = new OutputStreamWriter(new FileOutputStream(outFile), OUTPUT_CHARSET);
            BufferedWriter out = new BufferedWriter(w, OUTPUT_BUFFER_SIZE);
            result.add(new FilterOutputHelper(name, pattern, out));
            if (log.isDebugEnabled()) {
                log.debug("Filter loaded: [" + name + "][" + pattern + "]");
            }
        }
        return result;
    }

    private void cleanupFilters()
    {
        for (FilterOutputHelper filter :  filters) {
            try {
                filter.getOut().close();
            }
            catch (IOException e) {
                log.debug("Error during closing output of Filter[" + filter.getName() + "]");
            }
        }
    }

}
