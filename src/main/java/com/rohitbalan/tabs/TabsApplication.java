package com.rohitbalan.tabs;

import com.rohitbalan.tabs.model.Artist;
import com.rohitbalan.tabs.services.ArtistDownloader;
import com.rohitbalan.tabs.services.ArtistSearcher;
import com.rohitbalan.tabs.services.TabProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TabsApplication {
	private final Logger logger = LoggerFactory.getLogger(TabsApplication.class);

	@Autowired
	private ArtistSearcher artistSearcher;
	@Autowired
	private ArtistDownloader artistDownloader;
	@Autowired
	private TabProcessor tabProcessor;

	private boolean downloadRawTabs = false;
	private boolean processTabs = true;


	public static void main(String[] args) {
		final ConfigurableApplicationContext context = SpringApplication.run(TabsApplication.class, args);
		context.getBean(TabsApplication.class).execute(args);
	}

	public void execute(final String[] args) {
		if(downloadRawTabs) {
			for(final String arg: args) {
				try {
					final Artist artist = artistSearcher.execute(arg);
					artistDownloader.execute(artist);
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}

			}
		}
		if(processTabs) {
			logger.info("Process ...");
			tabProcessor.executeRootFolder();
			logger.info("Completed ...");
		}
	}

}
