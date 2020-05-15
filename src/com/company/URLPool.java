package com.company;

import java.util.LinkedList;

public class URLPool
{
    private LinkedList<URLDepthPair> ViewedList;
    private LinkedList<URLDepthPair> UnViewedList;
    private LinkedList<URLDepthPair> BlockedList;

    public int WaitingThreads;

    private int MaxDepth;

    public URLPool(int Depth)
    {
        WaitingThreads = 0;
        ViewedList = new LinkedList<URLDepthPair>();
        UnViewedList = new LinkedList<URLDepthPair>();
        BlockedList = new LinkedList<URLDepthPair>();
        this.MaxDepth = Depth;
    }

    public LinkedList<URLDepthPair> GetViewedList()
    {
        return this.ViewedList;
    }

    public LinkedList<URLDepthPair> GetUnViewedList()
    {
        return this.UnViewedList;
    }

    public LinkedList<URLDepthPair> GetBlockedList()
    {
        return this.BlockedList;
    }

    public synchronized int GetWaitingThreads()
    {
        return WaitingThreads;
    }

    public synchronized boolean AddingToList(URLDepthPair Pair)
    {
        boolean check = false;
        if (Pair.GetDepth() < this.MaxDepth)
        {
            UnViewedList.addLast(Pair);
            check = true;
            if (WaitingThreads > 0)
                WaitingThreads--;
            this.notify();
        }
        else
        {
            BlockedList.add(Pair);
        }
        return check;
    }

    public synchronized URLDepthPair GettingNextPair()
    {
        URLDepthPair NewDepthPair = null;
        if (UnViewedList.size() == 0)
        {
            WaitingThreads++;
            try
            {
                this.wait();
            }
            catch (InterruptedException ex)
            {
                System.err.println("InterruptedException: " + ex.getMessage());
                return null;
            }

        }
        NewDepthPair = UnViewedList.removeFirst();
        ViewedList.add(NewDepthPair);
        return NewDepthPair;
    }
}
