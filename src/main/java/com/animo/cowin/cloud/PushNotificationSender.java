package com.animo.cowin.cloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PushNotificationSender implements BackgroundFunction<PubSubMessage>{

	private static final Logger logger = Logger.getLogger(PushNotificationSender.class.getName());

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
	public void accept(PubSubMessage payload, Context context) throws Exception {

		String message = new String(Base64.getDecoder().decode(payload.getData()));
		logger.info("Message received "+message);	


		JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
		int pincode = jsonObject.get("pincode").getAsInt();
		int minAgeLimit = jsonObject.get("minAgeLimit").getAsInt();
		String district = jsonObject.get("districtName").getAsString();

		Firestore db = initializeFirestore();
		sendPushNotification(db,pincode,minAgeLimit,district);		

	}


	private String sendPushNotification(Firestore db, int pincode, int minAgeLimit, String district) {

		try {
			String collection = getCollection();
			logger.info("Collection name is "+collection);
			
			ApiFuture<QuerySnapshot> queryByPincode = db.collection(collection)
					.whereArrayContains("preferred_pincode", pincode)
					.get();

			ApiFuture<QuerySnapshot> queryByDistrict = db.collection(collection)
					.whereEqualTo("district", district)
					.get();
			
			QuerySnapshot querySnapshotByPincode = queryByPincode.get();
			List<QueryDocumentSnapshot> documentsByPinCode = querySnapshotByPincode.getDocuments();
			logger.info("Documents by Pincode "+documentsByPinCode);

			QuerySnapshot querySnapshotByDistrict = queryByDistrict.get();
			List<QueryDocumentSnapshot> documentsByDistrict = querySnapshotByDistrict.getDocuments();
			logger.info("Documents by District "+documentsByDistrict);

			Set<QueryDocumentSnapshot> set = new LinkedHashSet<QueryDocumentSnapshot>(documentsByPinCode);
			set.addAll(documentsByDistrict);
			List<QueryDocumentSnapshot> combinedList = new ArrayList<>(set);


			for (QueryDocumentSnapshot document : combinedList) {

				logger.info("Document_Id: " + document.getId());
				logger.info("first_name: " + document.getString("first_name"));
				logger.info("user_id: " + document.getString("user_id"));
				
				String deviceToken = document.getString("device_token");
				logger.info("device_token: " + deviceToken);
				
				Message message = Message.builder()
						.putData("test_key", "test_value")
						.setToken(deviceToken)
						.build();
				logger.info("Going to send message "+message.toString());
				String response = FirebaseMessaging.getInstance().send(message);
				logger.info("Message Id is "+response);
				return response;
			}
		}catch (Exception e) {
			logger.log(Level.SEVERE,"Unable to read data ",e);
		}finally {
			if(db!=null) {
				try {
					db.close();
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Unable to close db connection ",e);
				}
			}
		}
		return null;

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
