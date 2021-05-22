package com.animo.cowin.cloud.test;

import static com.google.common.truth.Truth.assertThat;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.animo.cowin.cloud.PubSubMessage;
import com.animo.cowin.cloud.PushNotificationSender;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.testing.TestLogHandler;

@RunWith(MockitoJUnitRunner.class)
public class PushNotificationSenderTest {
	
	@Spy
	private PushNotificationSender pushNotificationSender ;
	
	@Mock
	PubSubMessage message;
	
	private String projectId;
	private String userCollection;
	private String credentialFilePath;
	
	private static final Logger logger = Logger.getLogger(PushNotificationSender.class.getName());

	private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

	@Before
	public void setUp() throws IOException {
		FileReader reader=new FileReader("src/test/resources/gcp-test.properties");  
	 
	    Properties p=new Properties();  
	    p.load(reader);  
	      
	    projectId = p.getProperty("project_id");  
	    userCollection = p.getProperty("user_collection_name");  
	    credentialFilePath = p.getProperty("credential_file_path");
	    
		logger.addHandler(LOG_HANDLER);
		LOG_HANDLER.clear();
	}

	@Test
	public void pushNotifSender_shouldPrintName() throws Exception {
		
		Mockito.when(pushNotificationSender.getProjectId()).thenReturn(projectId);
		Mockito.when(pushNotificationSender.getCollection()).thenReturn(userCollection);
		
		InputStream serviceAccount = new FileInputStream(credentialFilePath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		Mockito.doReturn(credentials).when(pushNotificationSender).getCredentials();
		
		String s = "{\"centerName\":\"Indoor Stadium G (18-44) Only\",\"centerId\":698016,\"pincode\":751024,\"districtName\":\"Khurda\",\"date\":\"19-05-2021\",\"minAgeLimit\":18,\"availableCapacity\":4}";
		Mockito.doReturn(Base64.getEncoder().encodeToString(s.getBytes())).when(message).getData();
		
		//message.setData(Base64.getEncoder().encodeToString("TestJohn".getBytes(StandardCharsets.UTF_8)));
		pushNotificationSender.accept(message, null);

		String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
		assertThat("TestJohn").isEqualTo(logMessage);
	}

}
