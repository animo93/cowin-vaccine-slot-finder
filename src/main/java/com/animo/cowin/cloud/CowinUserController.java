package com.animo.cowin.cloud;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	public boolean getCORSEnabled() {
		return Boolean.parseBoolean(System.getenv("ENABLE_CORS"));
	}

	public String getDistrictsCollection() {
		return System.getenv("DISTRICT_COLLECTION_NAME");
	}

	@Override
	public void service(HttpRequest request, HttpResponse response) throws Exception {
		Firestore db = null;

		if(getCORSEnabled()) {
			logger.info("CORS is enabled");
			
			response.appendHeader("Access-Control-Allow-Origin", "*");

			if ("OPTIONS".equals(request.getMethod())) {
				response.appendHeader("Access-Control-Allow-Methods", "POST");
				response.appendHeader("Access-Control-Allow-Headers", "Content-Type");
				response.appendHeader("Access-Control-Max-Age", "300");
				response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT);
				return;
			}
		}


		try {
			JsonElement requestParsed = gson.fromJson(request.getReader(), JsonElement.class);
			JsonObject requestJson = null;

			if (requestParsed != null && requestParsed.isJsonObject()) {
				requestJson = requestParsed.getAsJsonObject();
			}

			CowinUserBean cowinUserBean = populateUser(requestJson);
			db = initializeFirestore();

			validateAndInsertDetails(db,cowinUserBean,response);
			insertNewDistrictCache(db,requestJson);


		}catch (IllegalArgumentException ex) {
			response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
			PrintWriter writer = new PrintWriter(response.getWriter());
			writer.printf("{\"message\":\"Please check all the required Arguments "+ex.getMessage()+"\"}");
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to save Cowin User Details ",e);
			response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
			PrintWriter writer = new PrintWriter(response.getWriter());
			writer.printf("{\"message\":\"Internal sever error "+e.getMessage()+"\"}");
		}
	}

	private void insertNewDistrictCache(Firestore db, JsonObject requestJson) throws InterruptedException, ExecutionException {

		String districtCollection = getDistrictsCollection();
		logger.info("District Collection name is "+districtCollection);

		String districtName = requestJson.get("district").getAsJsonObject().get("district_name").getAsString();
		int districtId = requestJson.get("district").getAsJsonObject().get("district_id").getAsInt();

		logger.info("District Name is "+districtName+" and Id "+districtId);

		ApiFuture<QuerySnapshot> queryByDistrictIdAndName = db.collection(districtCollection)
				.whereEqualTo("district_id", districtId)
				.whereEqualTo("district_name", districtName)
				.get();


		QuerySnapshot querySnapshotByIdAndName = queryByDistrictIdAndName.get();
		List<QueryDocumentSnapshot> documentsByIdAndName = querySnapshotByIdAndName.getDocuments();
		logger.info("Size of documents "+querySnapshotByIdAndName.size());
		if(documentsByIdAndName.isEmpty()) {

			Map<String,Object> districtMap = new HashMap<String, Object>();
			districtMap.put("district_name", districtName);
			districtMap.put("district_id", districtId);

			String docId = saveCowinDistrict(db,districtMap);
			if(docId!=null) {
				logger.info("Added New district "+districtName);
			}
		}else {
			logger.info("District already existing..No need to add");
		}

	}

	private String saveCowinDistrict(Firestore db, Map<String,Object> districtMap) throws InterruptedException, ExecutionException {
		String docId = db.collection(getDistrictsCollection())
				.add(districtMap)
				.get()
				.getId();
		logger.info("District Document inserted with Id "+docId);
		return docId;
	}

	private void validateAndInsertDetails(Firestore db, CowinUserBean cowinUserBean, HttpResponse response) throws InterruptedException, ExecutionException, IOException {

		String collection = getCollection();
		logger.info("Collection name is "+collection);
		
		ApiFuture<QuerySnapshot> queryByToken = db.collection(collection)
				.whereEqualTo("device_token", cowinUserBean.getDeviceToken())
				.get();


		QuerySnapshot querySnapshotByToken = queryByToken.get();
		List<QueryDocumentSnapshot> documentsByToken = querySnapshotByToken.getDocuments();
		logger.info("Size of documents "+querySnapshotByToken.size());
		if(documentsByToken.isEmpty()) {
			String docId = saveCowinUserDetails(db,cowinUserBean);
			if(docId!=null) {
				response.setStatusCode(HttpURLConnection.HTTP_CREATED);
				PrintWriter writer = new PrintWriter(response.getWriter());
				writer.printf("{\"message\":\"User Added successfully\"}");
			}
		}else {
			String foundDocumentId = documentsByToken.get(0).getId();
			updateCowinUserDetails(db, cowinUserBean, foundDocumentId);

			response.setStatusCode(HttpURLConnection.HTTP_OK);
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
			cowinUserBean.setDistrict(requestJson.get("district").getAsJsonObject().get("district_name").getAsString());
			cowinUserBean.setEmailAddress(requestJson.get("emailAddress").getAsString());
			cowinUserBean.setPinCode1(requestJson.get("pinCode1").getAsInt());
			cowinUserBean.setPinCode2(requestJson.get("pinCode2").getAsInt());
			cowinUserBean.setState(requestJson.get("state").getAsString());
			cowinUserBean.setDose(requestJson.get("dose").getAsString());
			cowinUserBean.setVaccine(requestJson.get("vaccine").getAsString());

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
