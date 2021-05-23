package com.animo.cowin.cloud.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.animo.cowin.cloud.CowinUserController;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import com.google.common.truth.Truth;

@RunWith(MockitoJUnitRunner.class)
public class CowinUserControllerTest {
	
	@Spy
	private CowinUserController cowinUserController;
	
	private String projectId;
	private String userCollection;
	private String credentialFilePath;
	
	@Mock
	private HttpRequest request;
	@Spy
	private HttpResponse response;
	
	private static final Logger logger = Logger.getLogger(CowinUserController.class.getName());

	private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

	@Before
	public void setUp() throws IOException {
		FileReader reader=new FileReader("src/test/resources/gcp-test.properties");  
	 
	    Properties p=new Properties();  
	    p.load(reader);  
	      
	    projectId = p.getProperty("project_id");  
	    userCollection = p.getProperty("user_collection_name");  
	    credentialFilePath = p.getProperty("credential_file_path");
	    
	    Mockito.when(cowinUserController.getProjectId()).thenReturn(projectId);
		Mockito.when(cowinUserController.getCollection()).thenReturn(userCollection);
		
		InputStream serviceAccount = new FileInputStream(credentialFilePath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		Mockito.doReturn(credentials).when(cowinUserController).getCredentials();
	    
		logger.addHandler(LOG_HANDLER);
		LOG_HANDLER.clear();
	}
	@Ignore
	@Test
	public void service_shouldReturn201() throws Exception {
		String requestString = "{\"name\":\"Prateek Singhdeo\",\"emailAddress\":\"abc@gmail.com\",\"pinCode1\":751004,\"pinCode2\":751003,\"ageLimit\":45,\"state\":\"Odisha\",\"district\":\"Khurdha\",\"deviceToken\":\"eVKp8OWIJzqlhJ\"}";
		String requestString2 = "{\"name\":\"Prateek Singhdeo\",\"emailAddress\":\"test@adf.com\",\"pinCode1\":723045,\"pinCode2\":723532,\"district\":\"Cuttack\",\"state\":\"Odisha\",\"deviceToken\":\"23324323erewrw\",\"ageLimit\":45}";
		BufferedReader reader = new BufferedReader(new StringReader(requestString));
		BufferedReader reader2 = new BufferedReader(new StringReader(requestString));
		Mockito.doReturn(reader).when(request).getReader();
		cowinUserController.service(request, response);
		
		Mockito.doReturn(reader2).when(request).getReader();
		cowinUserController.service(request, response);
	}
	
	@Test
	//@Ignore
	public void service_shouldReturn400() throws Exception {
		String requestString = "{\"emailAddress\":\"test@adf.com\",\"pinCode1\":\"723045\",\"pinCode2\":\"723532\",\"district\":\"Cuttack\",\"state\":\"Odisha\",\"deviceToken\":\"23324323erewrw\",\"ageLimit\":\"45\"}";
		BufferedReader reader = new BufferedReader(new StringReader(requestString));
		Mockito.doReturn(reader).when(request).getReader();
		cowinUserController.service(request, response);
		//Truth.assertThat(response.getWriter()).
	}
}
