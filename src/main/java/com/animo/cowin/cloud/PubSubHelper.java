package com.animo.cowin.cloud;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

public class PubSubHelper {

	private static final Logger logger = Logger.getLogger(PubSubHelper.class.getName());
	
	public GoogleCredentials getCredentials() throws IOException {
		return GoogleCredentials.getApplicationDefault();
	}
	
	public String getProjectId() {
		return System.getenv("PROJECT_ID");
	}

	public String getTopicId() {
		return System.getenv("TOPIC_ID");
	}

	public Publisher initializePubSub() {
		TopicName topicName = TopicName.of(getProjectId(), getTopicId());
		Publisher publisher = null;

		try {
			// Create a publisher instance with default settings bound to the topic
			// create a credentials provider
			CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(getCredentials());
			

			// apply credentials provider when creating publisher
			publisher = Publisher.newBuilder(topicName).setCredentialsProvider(credentialsProvider).build();
			//publisher = Publisher.newBuilder(topicName).build();
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to build publisher ",e);
		}
		return publisher;
	}

	public String pushDataToPubSub(CowinPubSubMessage cowinPubSubMessage) throws InterruptedException {
		Publisher publisher = initializePubSub();
		
		try {					
			String message = cowinPubSubMessage.toJson();
			logger.info("Message to be sent "+message);
			
			ByteString data = ByteString.copyFromUtf8(message);
			PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

			ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
			String messageId = messageIdFuture.get();
			
			logger.info("Published message ID: " + messageId);
			return messageId;
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to push data to Pub Sub ",e);
		}finally {
			if (publisher != null) {
				// When finished with the publisher, shutdown to free up resources.
				publisher.shutdown();
				publisher.awaitTermination(1, TimeUnit.MINUTES);
			}
		}
		return null;

	}

}
