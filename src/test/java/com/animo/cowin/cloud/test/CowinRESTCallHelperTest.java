package com.animo.cowin.cloud.test;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.animo.cowin.cloud.CowinRESTCallHelper;
import com.animo.cowin.cloud.PubSubHelper;
import com.google.common.testing.TestLogHandler;

@RunWith(MockitoJUnitRunner.class)
public class CowinRESTCallHelperTest {
	
	private static final Logger logger = Logger.getLogger(CowinRESTCallHelper.class.getName());
	private static final TestLogHandler LOG_HANDLER = new TestLogHandler();
	
	@Spy
	private CowinRESTCallHelper cowinRESTCallHelper;
	
	@Mock
	private PubSubHelper pubSubHelper = new PubSubHelper();

	@Before
	public void setUp() throws IOException {
		
		logger.addHandler(LOG_HANDLER);
		LOG_HANDLER.clear();
	}
	
	@Test
	public void fetchVaccineSlotAvailable_shouldFetch() throws URISyntaxException, IOException, InterruptedException {
		Mockito.doReturn(pubSubHelper).when(cowinRESTCallHelper).getPubSubHelper();
		cowinRESTCallHelper.fetchVaccineSlotAvailable(457L);
		
		String logMessage = LOG_HANDLER.getStoredLogRecords().get(LOG_HANDLER.getStoredLogRecords().size()-1).getMessage();
		assertThat("No centers available for the date ").isEqualTo(logMessage);		
	}
	

}
