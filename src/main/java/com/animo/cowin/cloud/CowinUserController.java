package com.animo.cowin.cloud;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
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

			validateAndInsertDetails(db,cowinUserBean,response);
			
			
		}catch (IllegalArgumentException ex) {
			response.setStatusCode(400);
			PrintWriter writer = new PrintWriter(response.getWriter());
		    writer.printf("{\"message\":\"Please check all the required Arguments "+ex.getMessage()+"\"}");
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to save Cowin User Details ",e);
			response.setStatusCode(500);
			PrintWriter writer = new PrintWriter(response.getWriter());
		    writer.printf("{\"message\":\"Internal sever error "+e.getMessage()+"\"}");
		}
	}

	private void validateAndInsertDetails(Firestore db, CowinUserBean cowinUserBean, HttpResponse response) throws InterruptedException, ExecutionException, IOException {
		
		String collection = getCollection();
		logger.info("Collection name is "+collection);
		
		ApiFuture<QuerySnapshot> queryByEmail = db.collection(collection)
				.whereEqualTo("email_address", cowinUserBean.getEmailAddress())
				.get();

		
		QuerySnapshot querySnapshotByEmail = queryByEmail.get();
		List<QueryDocumentSnapshot> documentsByEmail = querySnapshotByEmail.getDocuments();
		logger.info("Size of documents "+querySnapshotByEmail.size());
		if(documentsByEmail.isEmpty()) {
			String docId = saveCowinUserDetails(db,cowinUserBean);
			if(docId!=null) {
				response.setStatusCode(201);
				PrintWriter writer = new PrintWriter(response.getWriter());
			    writer.printf("{\"message\":\"User Added successfully\"}");
			}
		}else {
			String foundDocumentId = documentsByEmail.get(0).getId();
			updateCowinUserDetails(db, cowinUserBean, foundDocumentId);
			
			response.setStatusCode(200);
			PrintWriter writer = new PrintWriter(response.getWriter());
		    writer.printf("{\"message\":\"User Updated successfully\"}");
		}
		
	}

	private void updateCowinUserDetails(Firestore db, CowinUserBean cowinUserBean, String foundDocumentId) throws InterruptedException, ExecutionException {
		db.collection(getCollection())
				.document(foundDocumentId)
				.update(cowinUserBean.getUserBeanMap())
				.get();
		logger.info("Document "+foundDocumentId+" updated successfully ");
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
			
			boolean hasBeenInitialized=false;
			List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
			for(FirebaseApp app : firebaseApps){
			    if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)){
			        hasBeenInitialized=true;
			        
			    }
			}
			if(!hasBeenInitialized) {
				FirebaseApp.initializeApp(options);
			}
				
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to get firebase client ",e);
		}


		Firestore db = FirestoreClient.getFirestore();
		return db;
	}

}
