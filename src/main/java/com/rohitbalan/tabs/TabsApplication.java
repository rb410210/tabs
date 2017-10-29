package com.rohitbalan.tabs;

import com.rohitbalan.tabs.services.ArtistSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TabsApplication {
	private static final Logger logger = LoggerFactory.getLogger(TabsApplication.class);

	public static void main(String[] args) {
		try {
			final ConfigurableApplicationContext context = SpringApplication.run(TabsApplication.class, args);
			context.getBean(ArtistSearcher.class).execute(args[0]);
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
