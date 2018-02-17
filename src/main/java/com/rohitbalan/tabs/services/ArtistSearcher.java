package com.rohitbalan.tabs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.model.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class ArtistSearcher {

    private final Logger logger = LoggerFactory.getLogger(ArtistSearcher.class);

    @Autowired
    private Downloader downloader;

    public Artist execute(final String artist) throws IOException, InterruptedException {
        logger.info("Starting search criteria: " + artist);

/*
        final InputStream stream = new ClassPathResource("/search-results-1.html").getInputStream();
        final String content = IOUtils.toString(stream, Charset.defaultCharset());
*/

        final String url = "https://www.ultimate-guitar.com/search.php?search_type=band&order=&value=" + URLEncoder.encode(artist, String.valueOf(StandardCharsets.US_ASCII));
        final String content = downloader.execute(url);
        return parseSearchResults(content);
    }

    private Artist parseSearchResults(final String content) throws IOException, InterruptedException {
        final Artist artist = new Artist();
        final Set<Tab> tabs = new LinkedHashSet<>();
        artist.setTabs(tabs);

        final Map<String, ?> searchJson = standardMatcherJson(content);
        if(searchJson!=null) {
            final String artistName = ((Map<String, Map<String, List<Map<String, String>>>>) searchJson).get("data").get("results").get(0).get("artist_name");
            final String artistUrl = ((Map<String, Map<String, List<Map<String, String>>>>) searchJson).get("data").get("results").get(0).get("artist_url");
            artist.setName(artistName);


            /*
            final InputStream stream = new ClassPathResource("/artist-url-content.html").getInputStream();
            final String artistUrlContent = IOUtils.toString(stream, Charset.defaultCharset());
            */
            final String artistUrlContent = downloader.execute(artistUrl);

            final Map<String, ?> artistJson = standardMatcherJson(artistUrlContent);
            if(artistJson!=null) {
                logger.debug("artistJson {}", artistJson);
                tabs.addAll(getTabs(artistJson));

                final List<Map<String, String>> pages = ((Map<String, Map<String, Map<String, List<Map<String, String>>> >>) artistJson).get("data").get("pagination").get("pages");
                logger.debug("pages {}", pages);

                for (int i = 0; i < pages.size() ; i++) {
                    if(i==0)
                        continue;
                    final Map<String, String> pageDetails = pages.get(i);
                    final String paginationUrl = pageDetails.get("url");
                    if(paginationUrl!=null) {
                        /*
                        final InputStream paginatedStream = new ClassPathResource("/artist-url-content.html").getInputStream();
                        final String paginatedUrlContent = IOUtils.toString(paginatedStream, Charset.defaultCharset());
                        */
                        final String paginatedUrlContent = downloader.execute("https://www.ultimate-guitar.com/" + paginationUrl);

                        final Map<String, ?> paginatedJson = standardMatcherJson(paginatedUrlContent);
                        if(paginatedJson!=null) {
                            logger.debug("paginatedJson {}", paginatedJson);
                            tabs.addAll(getTabs(paginatedJson));
                        }
                    }
                }

            }
        }
        return artist;
    }

    private Set<Tab> getTabs(final Map<String, ?> artistJson) {
        final Set<Tab> tabs = new LinkedHashSet<>();
        final List<Map<String, String>> othertabs = ((Map<String, Map<String, List<Map<String, String>>>>) artistJson).get("data").get("other_tabs");
        for(final Map<String, String> tabMap: othertabs) {
            final String marketingType = tabMap.get("marketing_type");
            if(!"TabPro".equals(marketingType)) {
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
