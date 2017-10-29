package com.rohitbalan.tabs.services;

import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.model.Tab;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArtistSearcher {

    private final Logger logger = LoggerFactory.getLogger(ArtistSearcher.class);

    @Autowired
    private Downloader downloader;

    public Artist execute(final String artist) throws IOException {
        final String url = "https://www.ultimate-guitar.com/search.php?search_type=band&order=&value=" + URLEncoder.encode(artist, String.valueOf(StandardCharsets.US_ASCII));
        final String content = downloader.execute(url);
        logger.debug("Content: " + content);
        final Document html = Jsoup.parse(content);
        return parseSearchResults(html);
    }

    private Artist parseSearchResults(final Document html) throws IOException {
        final Artist artist = new Artist();
        final List<Tab> tabs = new ArrayList<>();
        artist.setTabs(tabs);

        for (final Element tableElement : html.body().getElementsByClass("tresults")) {
            final String tableHtml = tableElement.html();
            logger.debug(tableHtml);
            for (final Element anchorElement : tableElement.getElementsByTag("a")) {
                final String artistName = anchorElement.text();
                final String link = anchorElement.attr("href");
                logger.info("Artist: " + artistName);
                artist.setName(artistName);
                downloadTabIndexes(link, tabs);
                logger.info("Total tabs found: " + tabs.size());
                logger.info("tabs: " + tabs);
                return artist;
            }
            return artist;
        }
        logger.error("No search results!!");
        return artist;
    }

    private void downloadTabIndexes(final String tabIndex, final List<Tab> tabs) throws IOException {
        final String content = downloader.execute("https://www.ultimate-guitar.com/" + tabIndex);
        logger.debug("Content: " + content);
        final Document html = Jsoup.parse(content);
        for (final Element anchorElement : html.body().getElementsByTag("a")) {
            final String text = anchorElement.text();
            final String link = anchorElement.attr("href");
            if(link.contains("https://tabs.ultimate-guitar.com/")) {
                tabs.add(new Tab(text, link));
            } else if ("Next »".equals(text)) {
                downloadTabIndexes(link, tabs);
            }
            logger.debug(text + " " + link);
        }
    }
}
