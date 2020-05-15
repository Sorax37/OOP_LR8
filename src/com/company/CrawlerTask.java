package com.company;
import java.util.*;

public class CrawlerTask implements Runnable
{
    private URLPool Pool;
    private URLDepthPair Pair;

    public CrawlerTask(URLPool pool)
    {
        this.Pool = pool;
    }

    public void run()
    {
        Pair = Pool.GettingNextPair();
        int Depth = Pair.GetDepth();
        LinkedList<URLDepthPair> Links = new LinkedList<URLDepthPair>();
        Links = WebCrawler.GetSites(Pair);
        for (URLDepthPair pair: Links) {
            Pool.AddingToList(pair);
        }
    }
}
