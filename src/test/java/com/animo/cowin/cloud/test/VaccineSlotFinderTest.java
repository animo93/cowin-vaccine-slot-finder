package com.animo.cowin.cloud.test;

import static com.google.common.truth.Truth.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.animo.cowin.cloud.PubSubMessage;
import com.animo.cowin.cloud.VaccineSlotFinder;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.testing.TestLogHandler;

@RunWith(JUnit4.class)
public class VaccineSlotFinderTest {

	private VaccineSlotFinder vaccineFinder;
	private static final Logger logger = Logger.getLogger(VaccineSlotFinder.class.getName());

	private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

	@Before
	public void setUp() throws IOException {

		InputStream serviceAccount = new FileInputStream("/Users/animo/Downloads/elite-truck-313806-cbed7c3a8a19.json");
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

		logger.addHandler(LOG_HANDLER);
		LOG_HANDLER.clear();
	}

	@Test
	public void vaccineFinder_shouldPrintName() throws Exception {
		PubSubMessage message = new PubSubMessage();
		message.setData(Base64.getEncoder().encodeToString("TestJohn".getBytes(StandardCharsets.UTF_8)));
		vaccineFinder.accept(message, null);

		String logMessage = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
		assertThat("Hello John!").isEqualTo(logMessage);
	}

}
