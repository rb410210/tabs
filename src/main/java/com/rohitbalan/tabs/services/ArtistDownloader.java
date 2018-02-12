package com.rohitbalan.tabs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.model.Tab;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        artistFolder.mkdirs();
        if(!artistFolder.exists()) {
            throw new RuntimeException("Artist Folder not created.");
        }
        return artistFolder;
    }

    private void downloadTab(final Tab tab, final File folder) throws IOException, InterruptedException {
        final File tabFile = computeTabFile(tab, folder);

        if(tabFile.exists()) {
            logger.info("Was previously downloaded: {}", tab.getName());
        }

        /*
        final InputStream stream = new ClassPathResource("/tab-content.html").getInputStream();
        final String content = IOUtils.toString(stream, Charset.defaultCharset());
        */
        final String content = downloader.execute(tab.getUri());

        logger.debug("Content: {}", content);
        final Map<String, ?> tabJson = standardMatcherJson(content);
        final String tabContent = ((Map<String, Map<String, Map<String, Map<String, String>>>>) tabJson).get("data").get("tab_view").get("wiki_tab").get("content");

        final StringBuilder logStatus = new StringBuilder();
        logStatus.append("Downloading - ");
        logStatus.append(tab.getName());
        logStatus.append(" : ");

        FileCopyUtils.copy(tabContent.getBytes(StandardCharsets.UTF_8), tabFile);
        if(tabFile.exists()) {
            logStatus.append("COMPLETED");
        } else {
            logStatus.append("FAILED");
        }
        logger.info(logStatus.toString());
    }

    private Map<String, ?> standardMatcherJson(final String content) throws IOException {
        final Pattern pattern = Pattern.compile("(.*)(window.UGAPP.store.page = )([{].*[}])(</script>)");
        final Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String json = matcher.group(3);
            final Map<String, ?> parsedObject = new ObjectMapper().readValue(json, Map.class);
            return parsedObject;
        }
        return null;
    }


    private File computeTabFile(final Tab tab, final File folder) {
        final File tabFile = new File(folder, tab.getName() + ".txt");
        return tabFile;
    }
}
