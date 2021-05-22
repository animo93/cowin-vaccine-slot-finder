package com.animo.cowin.cloud;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
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

public class VaccineSlotFinder implements BackgroundFunction<PubSubMessage>{

	private static final Logger logger = Logger.getLogger(VaccineSlotFinder.class.getName());

	public GoogleCredentials getCredentials() throws IOException {
		return GoogleCredentials.getApplicationDefault();
	}

	public String getProjectId() {
		return System.getenv("PROJECT_ID");
	}

	public String getCollection() {
		return System.getenv("DISTRICT_COLLECTION_NAME");
	}

	public CowinRESTCallHelper getCowinRESTCallHelper() {
		return new CowinRESTCallHelper();
	}


	@Override
	public void accept(PubSubMessage message, Context context) throws Exception {		
		String data = message.data != null
				? new String(Base64.getDecoder().decode(message.data))
						: "Hello, World";
				logger.info(data);
				readData(initializeFirestore());

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

	private void readData(Firestore db) throws InterruptedException, ExecutionException {

		try {
			String collection = getCollection();
			logger.info("Collection name is "+collection);
			
			ApiFuture<QuerySnapshot> query = db.collection(collection).get();
			
			QuerySnapshot querySnapshot = query.get();
			List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
			for (QueryDocumentSnapshot document : documents) {

				logger.info("Document_Id: " + document.getId());
				logger.info("district_name: " + document.getString("district_name"));
				logger.info("district_id: " + document.getLong("district_id"));

				getCowinRESTCallHelper().fetchVaccineSlotAvailable(document.getLong("district_id"));

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

	}



}