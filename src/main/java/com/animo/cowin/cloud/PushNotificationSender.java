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
import com.google.firebase.messaging.Notification;
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
		

		Firestore db = initializeFirestore();
		sendPushNotification(db,jsonObject);		

	}


	private String sendPushNotification(Firestore db, JsonObject messageObject) {

		try {
			int pincode = messageObject.get("pincode").getAsInt();
			int minAgeLimit = messageObject.get("minAgeLimit").getAsInt();
			String vaccine = messageObject.get("vaccine").getAsString();
			int availableCapacityDose1 = messageObject.get("availableCapacityDose1").getAsInt();
			int availableCapacityDose2 = messageObject.get("availableCapacityDose2").getAsInt();
			String centerName = messageObject.get("centerName").getAsString();
			String date = messageObject.get("date").getAsString();
			String centerId = messageObject.get("centerId").getAsString();
			
			
			String collection = getCollection();
			
			logger.info("Collection name is "+collection);
			
			Set<QueryDocumentSnapshot> set = new LinkedHashSet<QueryDocumentSnapshot>();
			
			if(availableCapacityDose1>0) {
				ApiFuture<QuerySnapshot> queryByDose1 = db.collection(collection)
						.whereArrayContains("preferred_pincode", pincode)
						.whereEqualTo("age_limit", minAgeLimit)
						.whereEqualTo("vaccine", vaccine)
						.whereEqualTo("dose", "Dose1")
						.get();
				
				QuerySnapshot querySnapshotByDose1 = queryByDose1.get();
				List<QueryDocumentSnapshot> documentsByDose1 = querySnapshotByDose1.getDocuments();
				logger.info("Documents by Dose1 "+documentsByDose1);
				
				set.addAll(documentsByDose1);
			}
			
			if(availableCapacityDose2>0) {
				ApiFuture<QuerySnapshot> queryByDose2 = db.collection(collection)
						.whereArrayContains("preferred_pincode", pincode)
						.whereEqualTo("age_limit", minAgeLimit)
						.whereEqualTo("vaccine", vaccine)
						.whereEqualTo("dose", "Dose2")
						.get();
				
				QuerySnapshot querySnapshotByDose2 = queryByDose2.get();
				List<QueryDocumentSnapshot> documentsByDose2 = querySnapshotByDose2.getDocuments();
				logger.info("Documents by Dose2 "+documentsByDose2);
				
				set.addAll(documentsByDose2);
			}

			List<QueryDocumentSnapshot> combinedList = new ArrayList<>(set);


			for (QueryDocumentSnapshot document : combinedList) {

				logger.info("Document_Id: " + document.getId());
				logger.info("first_name: " + document.getString("first_name"));
				//logger.info("user_id: " + document.getString("user_id"));
				
				String deviceToken = document.getString("device_token");
				//logger.info("device_token: " + deviceToken);
				
				Notification notification = Notification.builder()
						.setTitle("Cowin Slots Available")
						.setBody(createNotificationBody(centerName,date))
						.build();
				Message message = Message.builder()
						.setNotification(notification)
						.putData("centerId", centerId)
						.putData("centerName", centerName)
						.putData("date", date)
						.setToken(deviceToken)
						.build();
				logger.info("Going to send message "+message.toString());
				String response = FirebaseMessaging.getInstance().send(message);
				logger.info("Message Id is "+response);
				return response;
			}
		}catch (Exception e) {
			logger.log(Level.SEVERE,"Unable to read data ",e);
		}
		return null;

	}

	private String createNotificationBody(String centerName, String date) {
		StringBuilder builder = new StringBuilder();
		return builder.append("Cowin Slots Available At \n")
					.append("Center : "+centerName+" \n ")
					.append("For Date : "+date+" \n")
					.toString();
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
