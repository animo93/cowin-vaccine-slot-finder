package com.animo.cowin.cloud.test;

import static com.google.common.truth.Truth.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.animo.cowin.cloud.PubSubMessage;
import com.animo.cowin.cloud.VaccineSlotFinder;
import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.testing.TestLogHandler;

@RunWith(MockitoJUnitRunner.class)
public class VaccineSlotFinderTest {

	@Spy
	private VaccineSlotFinder vaccineFinder ;
	private String projectId;
	private String districtCollection;
	private String credentialFilePath;
	private static final Logger logger = Logger.getLogger(VaccineSlotFinder.class.getName());

	private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

	@Before
	public void setUp() throws IOException {
		FileReader reader=new FileReader("src/test/resources/gcp-test.properties");  
		 
	    Properties p=new Properties();  
	    p.load(reader);  
	      
	    projectId = p.getProperty("project_id");  
	    districtCollection = p.getProperty("district_collection_name");
	    credentialFilePath = p.getProperty("credential_file_path");

		logger.addHandler(LOG_HANDLER);
		LOG_HANDLER.clear();
	}

	@Test
	public void vaccineFinder_shouldPrintName() throws Exception {
		
		Mockito.when(vaccineFinder.getProjectId()).thenReturn(projectId);
		Mockito.when(vaccineFinder.getCollection()).thenReturn(districtCollection);
		
		InputStream serviceAccount = new FileInputStream(credentialFilePath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		Mockito.doReturn(credentials).when(vaccineFinder).getCredentials();
		Mockito.when(vaccineFinder.getCredentials()).thenReturn(credentials);
		
		PubSubMessage message = new PubSubMessage();
		message.setData(Base64.getEncoder().encodeToString("TestJohn".getBytes(StandardCharsets.UTF_8)));
		vaccineFinder.accept(message, null);

		String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
		assertThat("TestJohn").isEqualTo(logMessage);
	}

}
