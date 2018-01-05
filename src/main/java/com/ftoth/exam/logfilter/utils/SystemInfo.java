package com.ftoth.exam.logfilter.utils;

public class SystemInfo
{
    public static int getCpuCoreCount()
    {
        return Runtime.getRuntime().availableProcessors();
    }


}
