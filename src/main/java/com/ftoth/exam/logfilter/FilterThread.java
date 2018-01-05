package com.ftoth.exam.logfilter;

import com.ftoth.exam.logfilter.config.FilterOutputHelper;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;

public class FilterThread implements Runnable
{
    private static Logger log = Logger.getLogger(FilterThread.class);

    private static final int INPUT_BUFFER_SIZE = 1024 * 1024;
    private static final String INPUT_CHARSET = "windows-1252";


    private int id;
    private String inFile;
    private List<FilterOutputHelper> filters;
    private CountDownLatch threadCompletionSignalCounter;

    //------------------------------ startup ----------------------------------
    public FilterThread(int id, String inFile, List<FilterOutputHelper> filters, CountDownLatch threadCompletionSignalCounter)
    {
        this.id = id;
        this.inFile = inFile;
        this.filters = filters;
        this.threadCompletionSignalCounter = threadCompletionSignalCounter;
    }


    @Override
    public void run()
    {
        FileInputStream is = null;
        BufferedReader reader = null;

        try {
            is = new FileInputStream(inFile);
            reader = new BufferedReader(new InputStreamReader(is, INPUT_CHARSET), INPUT_BUFFER_SIZE);

            // read
            String line = reader.readLine();
            while(line != null){
                processLine(line);
                line = reader.readLine();
            }

        } catch (FileNotFoundException e) {
            log.error("[" + inFile + "] : file not found", e);
        } catch (IOException e) {
            log.error("[" + inFile + "] : error during processing input file", e);

        } finally {
            try {
                reader.close();
                is.close();
            } catch (IOException e) {
                log.error("[" + inFile + "] : error during closing input file", e);
            }
            threadCompletionSignalCounter.countDown();
        }
    }

    private void processLine(String line) throws IOException
    {
        for (FilterOutputHelper filter : filters) {
            Matcher m = filter.getPattern().matcher(line);
            if (m.matches()) {
                filter.found(line);
                if (log.isDebugEnabled()) {
                    log.debug("FOUND:" +  filter.getName() + ", " + filter.getPatternValue());
                }
            }
        }
    }


    //------------------------------ helpers ----------------------------------
    public void sym()
    {
        if (log.isDebugEnabled()) {
            log.debug("FilterThread[" + id + "] processing input file: " + inFile);
        }

        int idx = 0;
        for (FilterOutputHelper filter : filters) {

            if (log.isDebugEnabled()) {
                log.debug("FilterThread[" + id + "] processing filter[ " + idx + "]:" + filter.getName() + " [" + filter.getPatternValue() + "]");
            }
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            idx++;
        }
    }
}
