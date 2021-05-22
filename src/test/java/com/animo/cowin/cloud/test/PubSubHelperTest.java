package com.animo.cowin.cloud.test;

import static com.google.common.truth.Truth.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.animo.cowin.cloud.CowinPubSubMessage;
import com.animo.cowin.cloud.PubSubHelper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.testing.TestLogHandler;

@RunWith(MockitoJUnitRunner.class)
public class PubSubHelperTest {
	
	private static final Logger logger = Logger.getLogger(PubSubHelper.class.getName());
	
	@Spy
	private PubSubHelper pubSubHelper;
	
	@Spy
	private CowinPubSubMessage message;

	private String projectId;

	private String credentialFilePath;

	private String slotAvailableTopic;

	private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

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
	public void pushDataToPubSub_shouldPushData() throws InterruptedException, FileNotFoundException, IOException {
		
		Mockito.when(pubSubHelper.getProjectId()).thenReturn(projectId);
		Mockito.when(pubSubHelper.getTopicId()).thenReturn(slotAvailableTopic);
		
		mockMessage();
		
		GoogleCredentials credentials = ServiceAccountCredentials
				.fromStream(new FileInputStream(credentialFilePath));
		Mockito.doReturn(credentials).when(pubSubHelper).getCredentials();
		
		String messageId = pubSubHelper.pushDataToPubSub(message);
		assertThat(messageId).isNotNull();
	}

	private void mockMessage() {
		Mockito.when(message.getAvailableCapacity()).thenReturn(10);
		Mockito.when(message.getCenterId()).thenReturn(18);
		Mockito.when(message.getCenterName()).thenReturn("TestCenter");
		Mockito.when(message.getDate()).thenReturn("15-05-2021");
		Mockito.when(message.getDistrictName()).thenReturn("Cuttack");
		Mockito.when(message.getMinAgeLimit()).thenReturn(18);
		Mockito.when(message.getPincode()).thenReturn(751024);
		
	}

}