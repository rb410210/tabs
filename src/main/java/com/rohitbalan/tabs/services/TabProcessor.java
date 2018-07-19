package com.rohitbalan.tabs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
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

@Service
public class TabProcessor {
    private final Logger logger = LoggerFactory.getLogger(TabProcessor.class);

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
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void processArtist(final String name) throws IOException {
        logger.info("Processing artist {}", name);
        final File processedTabsFolder = new File(processedTabs);
        final File rawArtistsFolder = new File(new File(downloadTo), name);
        final File processedArtistsFolder = new File(processedTabsFolder, name);
        processedArtistsFolder.mkdir();

        final File[] rawTabs = rawArtistsFolder.listFiles((dir, filename) -> filename.endsWith(".json") && !filename.startsWith("?"));

        for (final File rawTab : rawTabs) {
            final File processedTab = new File(processedArtistsFolder, rawTab.getName().replace(".json", ".txt"));
            final String jsonString = IOUtils.toString(new FileInputStream(rawTab), StandardCharsets.UTF_8);
            try {
                final String tabContent = getTabContent(jsonString);
                if (!StringUtils.isEmpty(tabContent)) {
                    FileCopyUtils.copy(tabContent.getBytes(StandardCharsets.UTF_8), processedTab);
                }
            } catch (Exception e) {
                logger.error("Unable to process " + name + " - " + rawTab.getName());
            }
        }

    }

    private String getTabContent(final String jsonString) throws IOException, TemplateException {
        final Map<String, ?> model = new ObjectMapper().readValue(jsonString, Map.class);
        final StringWriter out = new StringWriter();
        freeMarkerTemplate.process(model, out);
        return out.toString().replace("[ch]", "").replace("[/ch]", "");
    }


}
