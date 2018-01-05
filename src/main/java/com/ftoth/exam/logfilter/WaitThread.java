package com.ftoth.exam.logfilter;

public class WaitThread extends Thread
{
    private long waitTime;

    public WaitThread(long waitTime)
    {
        this.waitTime = waitTime;
    }

    @Override
    public void run()
    {
        try {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
