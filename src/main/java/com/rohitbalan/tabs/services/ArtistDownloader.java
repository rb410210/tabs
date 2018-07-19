package com.rohitbalan.tabs.services;

import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.model.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        if (artist != null && artist.getName() != null && !artist.getTabs().isEmpty()) {
            final File artistFolder = createFolder(artist);
            for (final Tab tab : artist.getTabs()) {
                downloadTab(tab, artistFolder);
            }
        }
    }


    private File createFolder(final Artist artist) {
        final File parentFolder = new File(downloadTo);
        if (!parentFolder.exists()) {
            throw new RuntimeException("Folder " + downloadTo + " is not present");
        }
        final File artistFolder = new File(parentFolder, artist.getName());
        artistFolder.mkdirs();
        if (!artistFolder.exists()) {
            throw new RuntimeException("Artist Folder not created.");
        }
        return artistFolder;
    }

    private void downloadTab(final Tab tab, final File folder) throws IOException, InterruptedException {
        final File jsonFile = computeTabFile(tab, folder, "json");
        final File rawHtmlFile = computeTabFile(tab, folder, "raw_html");

        if (rawHtmlFile.exists()) {
            logger.info("Was previously downloaded: {}", tab.getName());
            return;
        }

        /*
        final InputStream stream = new ClassPathResource("/tab-content.html").getInputStream();
        final String content = IOUtils.toString(stream, Charset.defaultCharset());
        */
        final String content = downloader.execute(tab.getUri());


        final StringBuilder logStatus = new StringBuilder();
        logStatus.append("Downloading - ");
        logStatus.append(tab.getName());
        logStatus.append(" : ");
        try {
            logger.debug("Content: {}", content);
            FileCopyUtils.copy(content.getBytes(StandardCharsets.UTF_8), rawHtmlFile);


            final String tabJson = standardMatcherJson(content);
            FileCopyUtils.copy(tabJson.getBytes(StandardCharsets.UTF_8), jsonFile);

            //String tabContent = ((Map<String, Map<String, Map<String, Map<String, String>>>>) tabJson).get("data").get("tab_view").get("wiki_tab").get("content");

        } catch (Exception error) {
            logger.error("Unable to download {}", tab.getUri());
        }

        if (jsonFile.exists()) {
            logStatus.append("COMPLETED");
        } else {
            logStatus.append("FAILED");
        }
        logger.info(logStatus.toString());
    }

    private String standardMatcherJson(final String content) throws IOException {
        final Pattern pattern = Pattern.compile("(.*)(window.UGAPP.store.page = )([{].*[}])([;]*)(</script>)*");
        final Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(3);
        }
        return null;
    }


    private File computeTabFile(final Tab tab, final File folder, final String ext) {
        final File tabFile = new File(folder, tab.getName() + "." + ext);
        return tabFile;
    }
}
