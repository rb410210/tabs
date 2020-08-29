package com.rohitbalan.tabs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
public class TabProcessor {

    private Template freeMarkerTemplate;

    @Value("${com.rohitbalan.tabs.processedTabs}")
    private String processedTabs;

    @Value("${com.rohitbalan.tabs.downloadTo}")
    private String downloadTo;

    @PostConstruct
    private void init() throws IOException {
        final Configuration freeMarkerConfiguration = new Configuration(new Version("2.3.23"));
        freeMarkerConfiguration.setClassForTemplateLoading(TabProcessor.class, "/templates/");
        freeMarkerConfiguration.setDefaultEncoding("UTF-8");

        freeMarkerTemplate = freeMarkerConfiguration.getTemplate("tab-text.ftl");
    }

    public void executeRootFolder() {
        final File processedTabsFolder = new File(processedTabs);
        if (!processedTabsFolder.exists()) {
            processedTabsFolder.mkdirs();
        }
        final File rawTabsFolder = new File(downloadTo);
        final File[] artists = rawTabsFolder.listFiles(pathname -> pathname.isDirectory());
        for (final File artist : artists) {
            try {
                processArtist(artist.getName());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void processArtist(final String name) throws IOException {
        log.info("Processing artist {}", name);
        final File processedTabsFolder = new File(processedTabs);
        final File rawArtistsFolder = new File(new File(downloadTo), name);
        final File processedArtistsFolder = new File(processedTabsFolder, name);
        processedArtistsFolder.mkdir();

        final File[] rawTabs = rawArtistsFolder.listFiles((dir, filename) -> filename.endsWith(".json") && !filename.startsWith("?"));

        for (final File rawTab : rawTabs) {
            final String jsonString = IOUtils.toString(new FileInputStream(rawTab), StandardCharsets.UTF_8);
            final Map<String, ?> model = new ObjectMapper().readValue(jsonString, Map.class);

            final File processedTab = getProcessedTabFile(processedArtistsFolder, rawTab, model);
            try {
                final String tabContent = getTabContent(model);
                if (!StringUtils.isEmpty(tabContent)) {
                    FileCopyUtils.copy(tabContent.getBytes(StandardCharsets.UTF_8), processedTab);
                }
            } catch (Exception e) {
                log.error("Unable to process " + name + " - " + rawTab.getName());
            }
        }

    }

    private File getProcessedTabFile(final File processedArtistsFolder, final File rawTab, final Map<String,?> model) {
        String name = rawTab.getName().replace(".json", ".txt");

        try {
            final String songName = ((Map<String,Map<String,Map<String,String>>>) model).get("data").get("tab").get("song_name");
            final String type = ((Map<String,Map<String,Map<String,String>>>) model).get("data").get("tab").get("type");
            final Integer id = ((Map<String,Map<String,Map<String,Integer>>>) model).get("data").get("tab").get("id");
            final Object objectRating = ((Map<String,Map<String,Map<String,Object>>>) model).get("data").get("tab").get("rating");
            final int rating;
            if(objectRating instanceof Double) {
                rating = Math.round(((Double)objectRating).floatValue());
            } else if (objectRating instanceof Float) {
                rating = Math.round((Float)objectRating);
            } else if (objectRating instanceof Integer) {
                rating = (int) objectRating;
            } else {
                rating = 0;
            }
            name = (songName + " - " + type + " - S" + rating + " - " + id).replace('/', '-') + ".txt";
        } catch (Exception e) {
            log.error("using default filename", e);
        }
        return new File(processedArtistsFolder, name);
    }

    private String getTabContent(final Map<String, ?> model) throws IOException, TemplateException {
        final StringWriter out = new StringWriter();
        freeMarkerTemplate.process(model, out);
        return out.toString().replace("[ch]", "").replace("[/ch]", "");
    }


}
