package com.rohitbalan.tabs;

import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.services.ArtistDownloader;
import com.rohitbalan.tabs.services.ArtistSearcher;
import com.rohitbalan.tabs.services.TabProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class TabsApplication implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(TabsApplication.class);

    @Autowired
    private ArtistSearcher artistSearcher;
    @Autowired
    private ArtistDownloader artistDownloader;
    @Autowired
    private TabProcessor tabProcessor;

    private boolean downloadRawTabs = true;
    private boolean processTabs = false;


    public static void main(String[] args) {
        SpringApplication.run(TabsApplication.class, args);
    }

    @Override
    public void run(final ApplicationArguments applicationArguments) throws Exception {
        if (applicationArguments.containsOption("page")) {
            downloadRawTabsFromArtistUrls(applicationArguments.getNonOptionArgs());
        } else {
            downloadRawTabsFromArtistNames(applicationArguments.getNonOptionArgs());
        }
        processTabs();
    }

    public void processTabs() {
        if (processTabs) {
            logger.info("Process ...");
            tabProcessor.executeRootFolder();
            logger.info("Completed ...");
        }
    }

    public void downloadRawTabsFromArtistNames(final List<String> args) {
        for (final String arg : args) {
            try {
                final Artist artist = artistSearcher.searchAndGenerateArtist(arg);
                artistDownloader.execute(artist);
                tabProcessor.processArtist(artist.getName());
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    public void downloadRawTabsFromArtistUrls(final List<String> urls) {
        for (final String url : urls) {
            try {
                final Artist artist = artistSearcher.generateArtistFromArtistHomePage(url);
                artistDownloader.execute(artist);
                tabProcessor.processArtist(artist.getName());
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

}
