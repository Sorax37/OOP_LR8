package com.company;

import java.util.*;
import java.io.*;
import java.net.*;

public class WebCrawler {
    public static String[] formats = {".html", ".pdf", ".doc", ".xml", ".java", ".txt", ".css", ".ico",".c", ".gif"};
    public static final String URL_PREFIX = "<a href=\"http://";
    public static final String URL_END = "/";
    public static final String URL_HREF = "<a href=\"";
    public static final int DEFAULT_THREADS = 3;

    int MaxDepth;
    int CountThreads;

    LinkedList<URLDepthPair> ViewedURLs;
    LinkedList<URLDepthPair> UnviewedURLs;

    public WebCrawler() {
        //ViewedURLs = new LinkedList<URLDepthPair>();
        //UnviewedURLs = new LinkedList<URLDepthPair>();
    }


    public static void main(String[] args) {
        WebCrawler Crawler = new WebCrawler();
        Crawler.CountThreads = WebCrawler.DEFAULT_THREADS;
        URLDepthPair FirstPair = Crawler.AddingFirstPair(args);
        Crawler.CountThreads = Crawler.GetCountOfThreads(args);
        //Crawler.GetSites();
        URLPool pool = new URLPool(Crawler.MaxDepth);
        pool.AddingToList(FirstPair);
        int thread = 0;
        int Active = Thread.activeCount();
        while (pool.GetWaitingThreads() != Crawler.CountThreads)
        {
            if (Thread.activeCount() - Active < Crawler.CountThreads)
            {
                CrawlerTask Task = new CrawlerTask(pool);
                new Thread(Task).start();
            }
            else
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException interex)
                {
                    System.err.println("InterruptedException: " + interex.getMessage());
                }
            }
        }
        System.out.println("Link search completed");
        LinkedList<URLDepthPair> ViewList = pool.GetViewedList();
        System.out.println("Viewed Pages:");
        for (URLDepthPair ViewPage : ViewList) {
            System.out.println(ViewPage.toString());
        }
        LinkedList<URLDepthPair> UnViewList = pool.GetBlockedList();
        for (URLDepthPair UnViewPage : UnViewList) {
            System.out.println(UnViewPage.toString());
        }
        System.exit(0);
    }

    private URLDepthPair AddingFirstPair(String args[]) {
        int StartDepth = 0;
        if (args.length != 3) {
            System.out.println("usage: java Crawler <URL> <depth> <Count of threads>");
            System.exit(1);
        } else {
            try {
                MaxDepth = Integer.parseInt(args[1]);//получение значения глубины из ввода пользователя
            } catch (NumberFormatException NumFormatEx) {
                System.out.println("usage: java Crawler <URL> <depth> <Count of threads>");
                System.exit(1);
            }
        }
        URLDepthPair FirstPair = new URLDepthPair(args[0], StartDepth);
        //UnviewedURLs.add(FirstPair);
        return FirstPair;
    }

    private int GetCountOfThreads(String args[])
    {
        int StartCount = 0;
        if (args.length != 3) {
            System.out.println("usage: java Crawler <URL> <depth> <Count of threads>");
            System.exit(1);
        } else {
            try {
                StartCount = Integer.parseInt(args[2]);
            } catch (NumberFormatException NumFormatEx) {
                System.out.println("usage: java Crawler <URL> <depth> <Count of threads>");
                System.exit(1);
            }
        }
        return StartCount;
    }

    private void URLDepthPairMovement(URLDepthPair Pair, Socket socket) {
        //перемещение сайта в список посещенных
        ViewedURLs.addLast(Pair);
        UnviewedURLs.removeFirst();
        //попытка закрытия сокета
        try {
            socket.close();
        } catch (UnknownHostException hostexe) {
            System.err.println("UnknownHostException: " + hostexe.getMessage());
        } catch (IOException ioexe) {
            System.err.println("IOException from socket: " + ioexe.getMessage());
        }
    }

    private static String TakeURLFromString(String InputURL, String str) {
        char[] ArrayURL = InputURL.toCharArray();
        int sign = 2;
        int index = InputURL.length();
        while (sign > 0 && index > 0)
        {
            index -= 1;
            if (ArrayURL[index] == '/')
                sign -= 1;
        }
        if (index == 0) return null;
        String CutURL = InputURL.substring(0, index + 1);
        String BackRef = str.substring(3, str.length());
        return (CutURL + BackRef);
    }

    public static String CutUsefulInf(String url)
    {
        int index = url.lastIndexOf("#");
        if (index == -1)
            return url;
        return url.substring(0, index);
    }

    public static String CutFileWithExtension(String url)
    {
        url = CutUsefulInf(url);
        for (String format : formats)
        {
            if (url.endsWith(format))
            {
                int lastCatalog = url.lastIndexOf("/");
                return url.substring(0, lastCatalog + 1);
            }
        }
        return url;
    }

    public static String TakeURLFromTag(String line)
    {
        if (line.indexOf(URL_HREF) == -1)
            return null;
        int indexStart = line.indexOf(URL_HREF) + URL_HREF.length();
        int indexEnd = line.indexOf("\"", indexStart);
        if (indexEnd == -1)
            return null;
        return line.substring(indexStart, indexEnd);
    }

    public static LinkedList<URLDepthPair> GetSites(URLDepthPair CurrentSite)
    {
        //URLDepthPair CurrentSite = UnviewedURLs.getFirst();
        LinkedList<URLDepthPair> ListOfLinks = new LinkedList<URLDepthPair>();
        Socket socket = null;
        try {
            //открытие сокета
            socket = new Socket(CurrentSite.GetHost(), 80);
            try
            {
                socket.setSoTimeout(3000);
            } catch (SocketException Socketex)
            {
                System.err.println("SocketException: " + Socketex.getMessage());
                //URLDepthPairMovement(CurrentSite, socket);
            }
            //вывод информации по текущей странице
            //URL CurrentSiteURL = new URL(CurrentSite.GetURL());//определение URL текущей страницы
            //System.out.println("--------------------------------------------------------");
            //System.out.println("Information about this page:");
            //System.out.println("URL of current page:" + CurrentSite.GetURL());
            //System.out.println("Path of current page:" + CurrentSiteURL.getPath());
            //System.out.println("Host of current page:" + CurrentSiteURL.getHost());
            //System.out.println("Protocol of current page:" + CurrentSiteURL.getProtocol());
            //System.out.println("File of current page:" + CurrentSiteURL.getFile());
            //System.out.println("Ref of current page:" + CurrentSiteURL.getRef());
            //System.out.println("--------------------------------------------------------");
            PrintWriter Writer = new PrintWriter(socket.getOutputStream(), true);
            // Отправка запроса для получения страницы
            Writer.println("GET " + CurrentSite.GetPath() + " HTTP/1.1");
            Writer.println("Host: " + CurrentSite.GetHost());
            Writer.println("Connection: close");
            Writer.println("");
            BufferedReader Reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Чтение первой строки результата запроса и проверка на bad request
            String line = Reader.readLine();
            if (!line.startsWith("HTTP/1.1 400 Bad Request"))
            {
                //System.out.println("The request is successful");
            } else {
                //System.out.println("Bad request");
                //System.out.println(line);
                //URLDepthPairMovement(CurrentSite, socket);
            }
            //Чтение результата запроса
           // System.out.println("Links on this page:");
            int counter = 0;
            while (line != null)
            {
                try
                {
                    line = Reader.readLine();
                    counter += 1;
                    //попробовать процедурой
                    String url = TakeURLFromTag(line);
                    if (url == null) continue;
                    if (url.startsWith("../"))
                    {
                        String NewURL = TakeURLFromString(CurrentSite.GetURL(), url);
                        //System.out.println("URL in string page:" +  NewURL + "\n");
                        URLDepthPair NewPair = new URLDepthPair(NewURL, CurrentSite.GetDepth() + 1);
                        ListOfLinks.addLast(NewPair);
                    }
                    else if (url.startsWith("http://")) {
                        String NewURL = CutUsefulInf(url);
                        //System.out.println("http page:" + NewURL + "\n");
                        URLDepthPair NewPair = new URLDepthPair(NewURL, CurrentSite.GetDepth() + 1);
                        ListOfLinks.addLast(NewPair);
                    }
                    else if (url.startsWith("https://")) {
                        //System.out.println("https page:" + url + " - it's https page" + "\n");
                        continue;
                    }
                    else {
                        String NewURL = CutFileWithExtension(CurrentSite.GetURL()) + url;
                        //System.out.println("Other page:" + NewURL + "\n");
                        URLDepthPair NewPair = new URLDepthPair(NewURL, CurrentSite.GetDepth() + 1);
                        ListOfLinks.addLast(NewPair);
                    }
                } catch (Exception e) {
                    break;
                }
            }
            if (counter == 1)
                //System.out.println("No http links on this page");
            System.out.println("--------------------------------------------------------");
        } catch (UnknownHostException hostex) {
            //System.err.println("UnknownHostException: " + hostex.getMessage());
            //URLDepthPairMovement(CurrentSite, socket);
            //continue;
        } catch (IOException ioex) {
            //System.err.println("IOException from socket: " + ioex.getMessage());
            //URLDepthPairMovement(CurrentSite, socket);
            //continue;
        }
        //URLDepthPairMovement(CurrentSite, socket);
        //CurrentSite = UnviewedURLs.getFirst();
        return ListOfLinks;
    }
}