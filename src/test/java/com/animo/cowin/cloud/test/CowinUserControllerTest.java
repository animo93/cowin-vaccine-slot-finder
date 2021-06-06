package com.animo.cowin.cloud.test;

import static com.google.common.truth.Truth.assertThat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@RunWith(MockitoJUnitRunner.class)
public class CowinUserControllerTest {
	
	@Spy
	private CowinUserController cowinUserController;
	
	private String projectId;
	private String userCollection;
	private String credentialFilePath;
	private boolean corsEnabled;
	private String districtCollection;
	
	@Mock
	private HttpRequest request;
	@Mock
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
	    corsEnabled = Boolean.parseBoolean(p.getProperty("cors_enabled"));
	    districtCollection = p.getProperty("district_collection_name");
	    
	    Mockito.when(cowinUserController.getProjectId()).thenReturn(projectId);
		Mockito.when(cowinUserController.getCollection()).thenReturn(userCollection);
		Mockito.when(cowinUserController.getCORSEnabled()).thenReturn(corsEnabled);
		Mockito.when(cowinUserController.getDistrictsCollection()).thenReturn(districtCollection);
		
		InputStream serviceAccount = new FileInputStream(credentialFilePath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		Mockito.doReturn(credentials).when(cowinUserController).getCredentials();
		
		Mockito.doReturn(new BufferedWriter(new Writer() {
			
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void flush() throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void close() throws IOException {
				// TODO Auto-generated method stub
				
			}
		})).when(response).getWriter();
	    
		logger.addHandler(LOG_HANDLER);
		LOG_HANDLER.clear();
	}
	@Ignore
	@Test
	public void service_shouldReturn201() throws Exception {
		String requestString = "{\"name\":\"Prateek Singhdeo\",\"emailAddress\":\"abc2@gmail.com\",\"pinCode1\":751004,\"pinCode2\":751003,\"ageLimit\":45,\"state\":\"Odisha\",\"district\":{\"district_name\":\"Khurda\",\"district_id\":446},\"deviceToken\":\"eVKp8OWIJzqlhJ\",\"dose\":\"Dose1\",\"vaccine\":\"COVAXIN\",\"searchBy\":\"district\"}";
		BufferedReader reader = new BufferedReader(new StringReader(requestString));		
		Mockito.doReturn(reader).when(request).getReader();
		
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();
		queryParams.put("ACTION", Arrays.asList("subscribe"));
		Mockito.doReturn(queryParams).when(request).getQueryParameters();
		
		cowinUserController.service(request, response);
		int lastLogIndex = LOG_HANDLER.getStoredLogRecords().size()-1;
		String lastLogMessage = LOG_HANDLER.getStoredLogRecords().get(lastLogIndex).getMessage();
		assertThat(lastLogMessage).matches("Document inserted with Id.*");
	}
	
	@Test
	@Ignore
	public void service_invalidBodyShouldReturn400() throws Exception {
		String requestString = "{\"emailAddress\":\"test@adf.com\",\"pinCode1\":\"723045\",\"pinCode2\":\"723532\",\"district\":\"Cuttack\",\"state\":\"Odisha\",\"deviceToken\":\"23324323erewrw\",\"ageLimit\":\"45\"}";
		BufferedReader reader = new BufferedReader(new StringReader(requestString));
		Mockito.doReturn(reader).when(request).getReader();
		
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();
		queryParams.put("ACTION", Arrays.asList("subscribe"));
		Mockito.doReturn(queryParams).when(request).getQueryParameters();
		
		cowinUserController.service(request, response);
		//Truth.assertThat(response.getWriter()).
	}
	@Ignore
	@Test
	public void service_existingUsershouldReturn200() throws Exception {
		String requestString = "{\"name\":\"Prateek Singhdeo\",\"emailAddress\":\"abc@gmail.com\",\"pinCode1\":751004,\"pinCode2\":751003,\"ageLimit\":45,\"state\":\"Odisha\",\"district\":{\"district_name\":\"Khurdha\",\"district_id\":446},\"deviceToken\":\"eVKp8OWIJzqlhJ\",\"dose\":\"Dose1\",\"vaccine\":\"COVAXIN\",\"searchBy\":\"district\"}";
		BufferedReader reader = new BufferedReader(new StringReader(requestString));
		Mockito.doReturn(reader).when(request).getReader();
		
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();
		queryParams.put("ACTION", Arrays.asList("subscribe"));
		Mockito.doReturn(queryParams).when(request).getQueryParameters();
		
		cowinUserController.service(request, response);
		
		int lastLogIndex = LOG_HANDLER.getStoredLogRecords().size()-1;
		String lastLogMessage = LOG_HANDLER.getStoredLogRecords().get(lastLogIndex).getMessage();
		assertThat(lastLogMessage).matches("Document.*updated successfully.*");
	}
	@Ignore
	@Test
	public void service_OptionsTypeshouldReturn204() throws Exception {
		String requestString = "{\"name\":\"Prateek Singhdeo\",\"emailAddress\":\"abc@gmail.com\",\"pinCode1\":751004,\"pinCode2\":751003,\"ageLimit\":45,\"state\":\"Odisha\",\"district\":\"Khurdha\",\"deviceToken\":\"eVKp8OWIJzqlhJ\"}";
		BufferedReader reader = new BufferedReader(new StringReader(requestString));		
		Mockito.doReturn(reader).when(request).getReader();
		
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();
		queryParams.put("ACTION", Arrays.asList("subscribe"));
		Mockito.doReturn(queryParams).when(request).getQueryParameters();
		
		Mockito.doReturn("OPTIONS").when(request).getMethod();
		cowinUserController.service(request, response);
		
		
		int lastLogIndex = LOG_HANDLER.getStoredLogRecords().size()-1;
		String lastLogMessage = LOG_HANDLER.getStoredLogRecords().get(lastLogIndex).getMessage();
		assertThat(lastLogMessage).matches("Document.*updated successfully.*");
	}
	
	//@Ignore
	@Test
	public void service_unsubscribeShouldReturn200() throws Exception {
		String requestString = "{\"deviceToken\":\"eVKp8OWIJzqlhJ\"}";
		BufferedReader reader = new BufferedReader(new StringReader(requestString));
		Mockito.doReturn(reader).when(request).getReader();
		
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();
		queryParams.put("ACTION", Arrays.asList("unsubscribe"));
		Mockito.doReturn(queryParams).when(request).getQueryParameters();
		
		cowinUserController.service(request, response);
		
		int lastLogIndex = LOG_HANDLER.getStoredLogRecords().size()-1;
		String lastLogMessage = LOG_HANDLER.getStoredLogRecords().get(lastLogIndex).getMessage();
		assertThat(lastLogMessage).matches("Document deleted successfully");
	}
	@Ignore
	@Test
	public void service_unsubscribeUnavailableTokenShouldReturn404() throws Exception {
		String requestString = "{\"deviceToken\":\"eVKp8OWIJzqlhJ\"}";
		BufferedReader reader = new BufferedReader(new StringReader(requestString));
		Mockito.doReturn(reader).when(request).getReader();
		
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();
		queryParams.put("ACTION", Arrays.asList("unsubscribe"));
		Mockito.doReturn(queryParams).when(request).getQueryParameters();
		
		cowinUserController.service(request, response);
		
		int lastLogIndex = LOG_HANDLER.getStoredLogRecords().size()-1;
		String lastLogMessage = LOG_HANDLER.getStoredLogRecords().get(lastLogIndex).getMessage();
		assertThat(lastLogMessage).matches("Document not found for deletion");
	}
}
