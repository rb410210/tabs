package com.rohitbalan.tabs.services;

import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.model.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ArtistDownloader {

    private final Logger logger = LoggerFactory.getLogger(ArtistDownloader.class);

    @Value("${com.rohitbalan.tabs.downloadTo}")
    private String downloadTo;

    public void execute(final Artist artist) {
        if(artist!=null && artist.getName()!=null && !artist.getTabs().isEmpty()) {
            final File artistFolder = createFolder(artist);
            for(final Tab tab: artist.getTabs()) {
                downloadTab(tab, artistFolder);
                break;
            }
        }
    }


    private File createFolder(final Artist artist) {
        final File parentFolder = new File(downloadTo);
        if(!parentFolder.exists()) {
            throw new RuntimeException("Folder " + downloadTo + " is not present");
        }
        final File artistFolder = new File(parentFolder, artist.getName());
        boolean artistFolderCreated = artistFolder.mkdir();
        if(!artistFolderCreated) {
            throw new RuntimeException("Unable to create artist folder");
        }
        return artistFolder;
    }

    private void downloadTab(final Tab tab, final File folder) {

    }
}
