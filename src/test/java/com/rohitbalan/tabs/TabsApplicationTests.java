package com.rohitbalan.tabs;

import com.rohitbalan.tabs.services.TabProcessor;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TabsApplicationTests {
	private static final Logger logger = LoggerFactory.getLogger(TabsApplicationTests.class);

	@Autowired
	private TabProcessor tabProcessor;

	@Test
	public void testBorns() throws IOException {
	}

}
