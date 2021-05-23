package com.animo.cowin.cloud;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CowinUserController implements HttpFunction{

	private static final Logger logger = Logger.getLogger(CowinUserController.class.getName());

	private static final Gson gson = new Gson();
	
	public GoogleCredentials getCredentials() throws IOException {
		return GoogleCredentials.getApplicationDefault();
	}

	public String getProjectId() {
		return System.getenv("PROJECT_ID");
	}

	public String getCollection() {
		return System.getenv("USER_COLLECTION_NAME");
	}

	@Override
	public void service(HttpRequest request, HttpResponse response) throws Exception {
		Firestore db = null;
		try {
			JsonElement requestParsed = gson.fromJson(request.getReader(), JsonElement.class);
			JsonObject requestJson = null;

			if (requestParsed != null && requestParsed.isJsonObject()) {
				requestJson = requestParsed.getAsJsonObject();
			}
			
			CowinUserBean cowinUserBean = populateUser(requestJson);
			db = initializeFirestore();
			
			String docId = saveCowinUserDetails(db,cowinUserBean);
			if(docId!=null) {
				response.setStatusCode(201);
			}
			
		}catch (IllegalArgumentException ex) {
			response.setStatusCode(400, "Please check all the required Arguments");
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to save Cowin User Details ",e);
		}finally {
			if(db!=null) {
				try {
					db.close();
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Unable to close db connection ",e);
				}
			}
		}
	}

	private String saveCowinUserDetails(Firestore db, CowinUserBean cowinUserBean) throws InterruptedException, ExecutionException {
		String docId = db.collection(getCollection())
				.add(cowinUserBean.getUserBeanMap())
				.get()
				.getId();
		logger.info("Document inserted with Id "+docId);
		return docId;
		
	}

	private CowinUserBean populateUser(JsonObject requestJson) {
		CowinUserBean cowinUserBean = new CowinUserBean();
		try {
			cowinUserBean.setName(requestJson.get("name").getAsString());
			cowinUserBean.setAgeLimit(requestJson.get("ageLimit").getAsInt());
			cowinUserBean.setDeviceToken(requestJson.get("deviceToken").getAsString());
			cowinUserBean.setDistrict(requestJson.get("district").getAsString());
			cowinUserBean.setEmailAddress(requestJson.get("emailAddress").getAsString());
			cowinUserBean.setPinCode1(requestJson.get("pinCode1").getAsInt());
			cowinUserBean.setPinCode2(requestJson.get("pinCode2").getAsInt());
			cowinUserBean.setState(requestJson.get("state").getAsString());
			
			return cowinUserBean;
		}catch (Exception e) {
			logger.log(Level.SEVERE,"Unable to populate User Bean",e);
			throw new IllegalArgumentException(e);
		}
	}
	
	private Firestore initializeFirestore() throws IOException {
		String projectId = getProjectId();
		logger.info("Project Id "+projectId);
		try {
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(getCredentials())
					.setProjectId(projectId)
					.build();
			FirebaseApp.initializeApp(options);
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to get firebase client ",e);
		}


		Firestore db = FirestoreClient.getFirestore();
		return db;
	}

}
