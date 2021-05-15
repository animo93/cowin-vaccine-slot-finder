package com.animo.cowin.cloud;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
	
	private GoogleCredentials credentials;

	public GoogleCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(GoogleCredentials credentials) {
		this.credentials = credentials;
	}


	@Override
	public void accept(PubSubMessage message, Context context) throws Exception {
		setCredentials(GoogleCredentials.getApplicationDefault());
		String data = message.data != null
				? new String(Base64.getDecoder().decode(message.data))
						: "Hello, World";
				logger.info(data);
		readData(initializeFirestore());

	}

	private Firestore initializeFirestore() throws IOException {
		// Use a service account
		
		
		//GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
		String projectId = System.getenv("PROJECT_ID");
		logger.info("Project Id "+projectId);
		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(getCredentials())
				.setProjectId(projectId)
				.build();
		FirebaseApp.initializeApp(options);

		Firestore db = FirestoreClient.getFirestore();
		return db;
	}

	private void readData(Firestore db) throws InterruptedException, ExecutionException {

		// asynchronously retrieve all users
		ApiFuture<QuerySnapshot> query = db.collection("cowin_district").get();
		// ...
		// query.get() blocks on response
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			logger.info("Document_Id: " + document.getId());
			logger.info("district_name: " + document.getString("district_name"));
			logger.info("district_id: " + document.getLong("district_id"));
			
		}
	}

}
