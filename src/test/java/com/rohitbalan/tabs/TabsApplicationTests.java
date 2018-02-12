package com.rohitbalan.tabs;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TabsApplicationTests {
	private static final Logger logger = LoggerFactory.getLogger(TabsApplicationTests.class);

	@Test
	@Ignore
	public void testU2() {
		final String[] args = {"U2"};
		TabsApplication.main(args);
	}

}
