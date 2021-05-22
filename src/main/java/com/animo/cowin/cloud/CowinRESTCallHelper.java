package com.animo.cowin.cloud;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.utils.URIBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CowinRESTCallHelper {
	
	private static final Logger logger = Logger.getLogger(CowinRESTCallHelper.class.getName());
	
	public PubSubHelper getPubSubHelper() {
		return new PubSubHelper();
	}
	
	public HttpClient getHttpClient() {
		return HttpClient.newBuilder()
				.build();
	}
	
	public void fetchVaccineSlotAvailable(Long districtId) throws URISyntaxException, IOException, InterruptedException {
		boolean datesAvailable = true;
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Calendar calendar = Calendar.getInstance();
		
		
		while(datesAvailable) {	
			String date = sdf.format(calendar.getTime()).toString();
			logger.info("Current Date "+date);
			URI uri = new URIBuilder("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict")
					.addParameter("district_id", String.valueOf(districtId))
					.addParameter("date", date)
					.build();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(uri)
					.GET()
					.header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36")
					.build();
			Consumer<? super HttpResponse<String>> responseAction = getResponseAction();
			HttpResponse<String> response = getHttpClient().send(request, BodyHandlers.ofString());
			calendar.add(Calendar.DATE, 7);
			datesAvailable = processResponse(response);
			
		}
		
	}
	
	public void getNextDates() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Calendar calendar = Calendar.getInstance();
		String date = sdf.format(calendar.getTime());
		logger.info("Current Date "+sdf.format(calendar.getTime()));
	}

	private boolean processResponse(HttpResponse<String> response) {
		logger.info("Response status code: "+ response.statusCode());
		if(response.statusCode()==200) {
			String responseBody = response.body();
			logger.info("Response body :"+responseBody);
			JsonObject jsonObj = JsonParser.parseString(responseBody).getAsJsonObject();
			JsonArray centerArray = jsonObj.get("centers").getAsJsonArray();
			if(centerArray.size()==0) {
				logger.info("No centers available for the date ");
				return false;
			}else {
				centerArray.forEach(filterCentersBySlotAvailability());
			}
		}
		return true;
	}

	private Consumer<? super HttpResponse<String>> getResponseAction() {
		return response -> {
			logger.info("Response status code: "+ response.statusCode());
			if(response.statusCode()==200) {
				String responseBody = response.body();
				logger.info("Response body :"+responseBody);
				JsonObject jsonObj = JsonParser.parseString(responseBody).getAsJsonObject();
				JsonArray centerArray = jsonObj.get("centers").getAsJsonArray();
				if(centerArray.size()==0) {
					logger.info("No centers available for the date");
				}else {
					centerArray.forEach(filterCentersBySlotAvailability());
				}
				
			}
			
		};
	}

	private Consumer<? super JsonElement> filterCentersBySlotAvailability() {
		return center -> {
			JsonArray sessionJsonArr = center.getAsJsonObject().get("sessions").getAsJsonArray();
			if(sessionJsonArr.size()>0) {
				sessionJsonArr.forEach(session -> {
					int availableCapacity = session.getAsJsonObject().get("available_capacity").getAsInt();
					logger.info("Available capacity for session "+session.getAsJsonObject().get("session_id")+" is "+availableCapacity);
					if(availableCapacity>0) {
						try {
							
							CowinPubSubMessage message = createPubSubMessage(center, session, availableCapacity);
							
							getPubSubHelper().pushDataToPubSub(message);
						} catch (InterruptedException e) {
							logger.log(Level.SEVERE, "Unable to push data to Pub Sub ",e);
						}
					}
				});
			}
		};
	}

	private CowinPubSubMessage createPubSubMessage(JsonElement center, JsonElement session, int availableCapacity) {
		CowinPubSubMessage message = new CowinPubSubMessage();
		message.setAvailableCapacity(availableCapacity);
		message.setCenterId(center.getAsJsonObject().get("center_id").getAsInt());
		message.setCenterName(center.getAsJsonObject().get("name").getAsString());
		message.setDate(session.getAsJsonObject().get("date").getAsString());
		message.setDistrictName(center.getAsJsonObject().get("district_name").getAsString());
		message.setMinAgeLimit(session.getAsJsonObject().get("min_age_limit").getAsInt());
		message.setPincode(center.getAsJsonObject().get("pincode").getAsInt());
		return message;
	}

	

}
