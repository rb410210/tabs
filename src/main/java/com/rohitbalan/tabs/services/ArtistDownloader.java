package com.rohitbalan.tabs.services;

import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.model.Tab;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ArtistDownloader {

    private final Logger logger = LoggerFactory.getLogger(ArtistDownloader.class);

    @Autowired
    private Downloader downloader;

    @Value("${com.rohitbalan.tabs.downloadTo}")
    private String downloadTo;

    public void execute(final Artist artist) throws IOException, InterruptedException {
        if(artist!=null && artist.getName()!=null && !artist.getTabs().isEmpty()) {
            final File artistFolder = createFolder(artist);
            for(final Tab tab: artist.getTabs()) {
                downloadTab(tab, artistFolder);
            }
        }
    }


    private File createFolder(final Artist artist) {
        final File parentFolder = new File(downloadTo);
        if(!parentFolder.exists()) {
            throw new RuntimeException("Folder " + downloadTo + " is not present");
        }
        final File artistFolder = new File(parentFolder, artist.getName());
        if(artistFolder.exists()) {
            throw new RuntimeException("Artist Folder already exists.");
        }
        boolean artistFolderCreated = artistFolder.mkdir();
        if(!artistFolderCreated) {
            throw new RuntimeException("Unable to create artist folder");
        }
        return artistFolder;
    }

    private void downloadTab(final Tab tab, final File folder) throws IOException, InterruptedException {
        final File tabFile = computeTabFile(tab, folder, 0);
        Thread.sleep(1000L);
        final String content = downloader.execute(tab.getUri());
        logger.debug("Content: " + content);

        final StringBuilder logStatus = new StringBuilder();
        logStatus.append("Downloading - ");
        logStatus.append(tab.getName());
        logStatus.append(" : ");

        final Document html = Jsoup.parse(content);

        for (final Element preElement : html.body().getElementsByTag("pre")) {
            if(preElement.hasClass("js-tab-content")) {
                final String tabData = preElement.text();
                logger.debug("tabData: " + tabData);
                FileCopyUtils.copy(tabData.getBytes(StandardCharsets.UTF_8), tabFile);
            }
        }

        if(tabFile.exists()) {
            logStatus.append("COMPLETED");
        } else {
            logStatus.append("FAILED");
        }
        logger.info(logStatus.toString());

    }

    private File computeTabFile(final Tab tab, final File folder, final int i) {
        final File tabFile = new File(folder, tab.getName() + (i==0? "" : (" - " + i)) + ".txt");
        if(tabFile.exists()) {
            return computeTabFile(tab, folder, i + 1);
        } else {
            return tabFile;
        }
    }
}
