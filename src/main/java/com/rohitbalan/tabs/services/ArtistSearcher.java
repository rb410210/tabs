package com.rohitbalan.tabs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.model.Tab;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ArtistSearcher {

    @Autowired
    private Downloader downloader;

    public Artist searchAndGenerateArtist(final String artist) throws IOException, InterruptedException {
        log.info("Starting search criteria: " + artist);

        final String url = "https://www.ultimate-guitar.com/search.php?search_type=band&order=&value=" + URLEncoder.encode(artist, String.valueOf(StandardCharsets.US_ASCII));
        final String content = downloader.execute(url);
        return parseSearchResults(content);
    }

    private Artist parseSearchResults(final String content) throws IOException, InterruptedException {

        final Map<String, ?> searchJson = standardMatcherJson(content);
        if (searchJson != null) {
            final String artistUrl = ((Map<String, Map<String, List<Map<String, String>>>>) searchJson).get("data").get("results").get(0).get("artist_url");

            return generateArtistFromArtistHomePage(artistUrl);
        }
        throw new RuntimeException("Unable to find artist");
    }

    public Artist generateArtistFromArtistHomePage(final String artistUrl) throws IOException, InterruptedException {
        final Artist artist = new Artist();
        final Set<Tab> tabs = new LinkedHashSet<>();
        artist.setTabs(tabs);

        final String artistUrlContent = downloader.execute(artistUrl);
        final Map<String, ?> artistJson = standardMatcherJson(artistUrlContent);
        if (artistJson != null) {
            log.debug("artistJson {}", artistJson);

            final String artistName = ((Map<String, Map<String, Map<String, String>>>) artistJson).get("data").get("artist").get("name");
            log.info("Artist Name: {}", artistName);
            artist.setName(artistName);
            tabs.addAll(getTabs(artistJson));

            final List<Map<String, String>> pages = ((Map<String, Map<String, Map<String, List<Map<String, String>>>>>) artistJson).get("data").get("pagination").get("pages");
            log.debug("pages {}", pages);

            for (int i = 0; i < pages.size(); i++) {
                if (i == 0)
                    continue;
                final Map<String, String> pageDetails = pages.get(i);
                final String paginationUrl = pageDetails.get("url");
                if (paginationUrl != null) {
                    final String paginatedUrlContent = downloader.execute("https://www.ultimate-guitar.com/" + paginationUrl);

                    final Map<String, ?> paginatedJson = standardMatcherJson(paginatedUrlContent);
                    if (paginatedJson != null) {
                        log.debug("paginatedJson {}", paginatedJson);
                        tabs.addAll(getTabs(paginatedJson));
                    }
                }
            }

        }
        return artist;
    }


    private Set<Tab> getTabs(final Map<String, ?> artistJson) {
        final Set<Tab> tabs = new LinkedHashSet<>();
        final List<Map<String, String>> othertabs = ((Map<String, Map<String, List<Map<String, String>>>>) artistJson).get("data").get("other_tabs");
        for (final Map<String, String> tabMap : othertabs) {
            final String marketingType = tabMap.get("marketing_type");
            if (!"TabPro".equals(marketingType)) {
                final String tabUrl = tabMap.get("tab_url");
                final String tabName = tabUrl.substring(tabUrl.lastIndexOf('/') + 1);
                final Tab tab = new Tab(tabName, tabUrl);
                tabs.add(tab);
            }

        }
        return tabs;
    }

    private Map<String, ?> standardMatcherJson(final String content) throws IOException {
        final Pattern pattern = Pattern.compile("(.*)(window.UGAPP.store.page = )([{].*[}])([;]*)(</script>)*");
        final Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String json = matcher.group(3);
            final Map<String, ?> parsedObject = new ObjectMapper().readValue(json, Map.class);
            return parsedObject;
        }
        return null;
    }


}
