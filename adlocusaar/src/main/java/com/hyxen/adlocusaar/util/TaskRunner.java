package com.hyxen.adlocusaar.util;

import java.util.LinkedList;

/**
 * Created by kiddchen on 2014/1/16.
 */
public class TaskRunner
{
    private final LinkedList<Runnable> mQueue = new LinkedList<>();

    private Thread mThread;
    private boolean isStart = false;

    private final Runnable mRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            Runnable r;
            while (mThread == Thread.currentThread() && mThread != null)
            {
                try
                {
                    synchronized (mQueue)
                    {
                        r = mQueue.poll();
                        if (r == null)
                        {
                            break;
                        }
                    }
                    r.run();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                Thread.yield();
            }
            stop();
        }
    };

    public void put(Runnable runnable)
    {
        synchronized (mQueue)
        {
            mQueue.offer(runnable);
            start();
        }
    }

    public boolean remove(Runnable runnable)
    {
        synchronized (mQueue)
        {
            return mQueue.remove(runnable);
        }
    }

    private synchronized void start()
    {
        if (isStart)
        {
            return;
        }
        if (mQueue.isEmpty())
        {
            return;
        }
        isStart = true;
        mThread = new Thread(mRunnable);
        mThread.start();
    }

    private synchronized void stop()
    {
        if (!isStart)
        {
            return;
        }
        isStart = false;
        mThread = null;
    }

}
