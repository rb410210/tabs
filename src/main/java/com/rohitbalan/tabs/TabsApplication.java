package com.rohitbalan.tabs;

import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.services.ArtistDownloader;
import com.rohitbalan.tabs.services.ArtistSearcher;
import com.rohitbalan.tabs.services.TabProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@SpringBootApplication
public class TabsApplication implements ApplicationRunner {

    @Autowired
    private ArtistSearcher artistSearcher;
    @Autowired
    private ArtistDownloader artistDownloader;
    @Autowired
    private TabProcessor tabProcessor;
    @Value("classpath:/help.txt")
    private Resource helpText;

    public static void main(String[] args) {
        SpringApplication.run(TabsApplication.class, args);
    }

    @Override
    public void run(final ApplicationArguments applicationArguments) throws Exception {
        if(applicationArguments.getSourceArgs()==null || applicationArguments.getSourceArgs().length==0) {
            final String usage =  IOUtils.toString(helpText.getInputStream(), StandardCharsets.UTF_8);
            log.info(usage);
        }

        if (applicationArguments.containsOption("page")) {
            downloadRawTabsFromArtistUrls(applicationArguments.getNonOptionArgs());
        } else {
            downloadRawTabsFromArtistNames(applicationArguments.getNonOptionArgs());
        }
        if (applicationArguments.containsOption("reprocess")) {
            processTabs();
        }
    }

    public void processTabs() {
        log.info("Process ...");
        tabProcessor.executeRootFolder();
        log.info("Completed ...");
    }

    public void downloadRawTabsFromArtistNames(final List<String> args) {
        for (final String arg : args) {
            try {
                final Artist artist = artistSearcher.searchAndGenerateArtist(arg);
                artistDownloader.execute(artist);
                tabProcessor.processArtist(artist.getName());
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
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
                log.error(e.getMessage(), e);
            }

        }
    }

}
