package com.animo.cowin.cloud.test;

import static com.google.common.truth.Truth.assertThat;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.animo.cowin.cloud.CowinRESTCallHelper;
import com.animo.cowin.cloud.PubSubHelper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.testing.TestLogHandler;

@RunWith(MockitoJUnitRunner.class)
public class FetchAndPushToPubSubIntegrationTest {
	
	private static final Logger logger = Logger.getLogger(CowinRESTCallHelper.class.getName());
	private static final TestLogHandler LOG_HANDLER = new TestLogHandler();
	
	@Spy
	private CowinRESTCallHelper cowinRESTCallHelper;
	
	@Spy
	private PubSubHelper pubSubHelper;
	private String projectId;
	private String credentialFilePath;
	private String slotAvailableTopic;

	@Before
	public void setUp() throws IOException {
		FileReader reader=new FileReader("src/test/resources/gcp-test.properties");  
		 
	    Properties p=new Properties();  
	    p.load(reader);  
	      
	    projectId = p.getProperty("project_id");   
	    credentialFilePath = p.getProperty("credential_file_path");
	    slotAvailableTopic = p.getProperty("slot_available_topic");
	    
		logger.addHandler(LOG_HANDLER);
		LOG_HANDLER.clear();
	}
	
	@Test
	public void fetchFromCowinAndPushToPubSubIntegrationTest() throws URISyntaxException, IOException, InterruptedException {
		Mockito.doReturn(pubSubHelper).when(cowinRESTCallHelper).getPubSubHelper();
		Mockito.when(pubSubHelper.getProjectId()).thenReturn(projectId);
		Mockito.when(pubSubHelper.getTopicId()).thenReturn(slotAvailableTopic);
		
		GoogleCredentials credentials = ServiceAccountCredentials
				.fromStream(new FileInputStream(credentialFilePath));
		Mockito.doReturn(credentials).when(pubSubHelper).getCredentials();
		
		cowinRESTCallHelper.fetchVaccineSlotAvailable(457L);
		
		String logMessage = LOG_HANDLER.getStoredLogRecords().get(LOG_HANDLER.getStoredLogRecords().size()-1).getMessage();
		assertThat("No centers available for the date ").isEqualTo(logMessage);		
	}

}
