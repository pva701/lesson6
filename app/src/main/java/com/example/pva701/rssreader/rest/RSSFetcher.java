package com.example.pva701.rssreader.rest;

import android.util.Log;

import com.example.pva701.rssreader.News;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by pva701 on 16.10.14.
 */


public class RSSFetcher {
    private static class NewsParseHandler extends DefaultHandler {
        private ArrayList <News> news;
        private ArrayList <String> stack;
        private ArrayList <String> textTag;
        public NewsParseHandler() {
            news = new ArrayList<News>();
            stack = new ArrayList<String>();
            textTag = new ArrayList<String>();
        }

        private News curNews;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("item"))
                curNews = new News();
            stack.add(qName.toLowerCase());
            textTag.add("");
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equalsIgnoreCase("item"))
                news.add(curNews);
            else if (curNews != null)
                put(curNews, stack.get(stack.size() - 1), textTag.get(textTag.size() - 1));
            stack.remove(stack.size() - 1);
            textTag.remove(textTag.size() - 1);
        }

        private Date parseDate(String value) {
            String monthLong[] = {"", "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
            String days[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            for (int i = 1; i < monthLong.length; ++i) {
                value = value.replace(monthLong[i] + ",", (i < 10 ? "0" + i : "" + i));
                value = value.replace(monthLong[i].substring(0, 3) + ",", (i < 10 ? "0" + i : "" + i));
                value = value.replace(monthLong[i], (i < 10 ? "0" + i : "" + i));
                value = value.replace(monthLong[i].substring(0, 3), (i < 10 ? "0" + i : "" + i));
            }
            for (int i = 0; i < 7; ++i) {
                value = value.replace(days[i] + ",", "");
                value = value.replace(days[i].substring(0, 3) + ",", "");
                value = value.replace(days[i], "");
                value = value.replace(days[i].substring(0, 3), "");
            }
            value = value.trim();
            int pos = value.indexOf("+");
            if (pos != -1)
                value = value.substring(0, pos) + (pos + 5 >= value.length() ? "" : value.substring(pos + 5));
            if (Character.isLetter(value.charAt(value.length() - 1)))
                value = value.substring(0, value.length() - 3);
            try {
                return new SimpleDateFormat("dd MM yyyy hh:mm:ss").parse(value);
            } catch (Exception e) {
                return null;
            }
        }

        public void put(News news, String key, String value) {
            if (key.equalsIgnoreCase("title"))
                news.setTitle(value.trim());
            else if (key.equalsIgnoreCase("link"))
                news.setLink(value);
            else if
                    (key.equalsIgnoreCase("description"))
                news.setDescription(value);
            else if (key.equalsIgnoreCase("pubDate"))
                news.setPubDate(parseDate(value));
            else if (key.equalsIgnoreCase("category"))
                news.addCategory(value);
        }

        public String trim(String s) {
            int i = 0;
            while (s.charAt(i) == ' ' || s.charAt(i) == ']') ++i;
            return s.substring(i, s.length());
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException, SAXException, IllegalArgumentException {
            textTag.set(textTag.size() - 1, textTag.get(textTag.size() - 1) + new String(ch, start, length));
        }

        public ArrayList <News> getNewsItems() {
            return news;
        }
    }

    public static InputStream getInputStream(String url) throws UnknownHostException {
        InputStream inputStream = null;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(20 * 1000);
            inputStream = connection.getInputStream();
        } catch (UnknownHostException e) {
            throw e;
        }
        catch (Exception e) {}
        return inputStream;
    }

    public static ArrayList<News> fetch(String... strings) throws UnknownHostException, SAXException, IllegalArgumentException {
        //Log.i("RSSFetcher", "in doInBackground");
        ArrayList <News> ret = new ArrayList<News>();
        for (int urlId = 0; urlId < strings.length; ++urlId) {
            String url = strings[urlId];

            try {
                String encoding = "";
                InputStream stream = getInputStream(url);
                BufferedReader in = new BufferedReader(new InputStreamReader(stream));
                String inpLine = in.readLine();
                while (inpLine.trim().equals("")) inpLine = in.readLine();
                in.close();

                int pos = inpLine.indexOf("encoding") + 10;
                while (inpLine.charAt(pos) != '\"'&& inpLine.charAt(pos) != '\'')
                    encoding += inpLine.charAt(pos++);
                stream.close();

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                NewsParseHandler handler = new NewsParseHandler();
                //InputSource inputSource = new InputSource(new StringReader(xml));*/
                Reader isr = new InputStreamReader(getInputStream(url), encoding);
                InputSource inputSource = new InputSource();
                inputSource.setCharacterStream(isr);
                saxParser.parse(inputSource, handler);
                ret.addAll(handler.getNewsItems());
            } catch (UnknownHostException e) {
                throw e;
            } catch (SAXException e) {
                throw e;
            } catch (StringIndexOutOfBoundsException e) {
                throw new SAXException();
            } catch (Exception e) {
                throw new IllegalArgumentException(url);
            }
        }
        return ret;
    }
}
