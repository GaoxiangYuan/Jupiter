package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import entity.Item;
//import entity.Item.ItemBuilder;

public class TicketMasterAPI {
	//if name on ticketmaster changed we just need to change one name.
	private static final String EMBEDDED = "_embedded";
	private static final String EVENTS = "events";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String URL_STR = "url";
	private static final String RATING = "rating";
	private static final String DISTANCE = "distance";
	private static final String VENUES = "venues";
	private static final String ADDRESS = "address";
	private static final String LINE1 = "line1";
	private static final String LINE2 = "line2";
	private static final String LINE3 = "line3";
	private static final String CITY = "city";
	private static final String IMAGES = "images";
	private static final String CLASSIFICATIONS = "classifications";
	private static final String SEGMENT = "segment";

	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "jEGwOOMW43uPkjcZaY8MPYaECdWXPmxp";
	
	 public List<Item> search(double lat, double lon, String keyword) {
         if(keyword == null) {
        	 keyword = DEFAULT_KEYWORD;
         }
         try {
        	 keyword = URLEncoder.encode(keyword, "UTF-8");
         }catch(Exception e) {
        	 e.printStackTrace();
         }
         String geoHash = GeoHash.encodeGeohash(lat, lon, 9);
         String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=50", API_KEY, geoHash, keyword);

 	    
 		try {
 			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
 			connection.setRequestMethod("GET");
 			//debug
 			int responseCode = connection.getResponseCode();
 			System.out.println("Sending 'GET' request to URL: " + URL);
 			System.out.println("Response Code: " + responseCode);
 			//turn response to JASON object
 			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			StringBuilder response = new StringBuilder();
 			
 			String inputLine;
 			while ((inputLine = in.readLine()) != null) {
 				response.append(inputLine);
 			}
 			in.close();
 			
 			JSONObject obj = new JSONObject(response.toString());
 			if (!obj.isNull("_embedded")) {
 				JSONObject embedded = obj.getJSONObject("_embedded");
 			    JSONArray events = embedded.getJSONArray("events");
 			    return getItemList(events);


 			}	
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return new ArrayList<>();
		
         
 

	 }
	 //Convert JSONArray to a list of Item Objects
	 private List<Item> getItemList(JSONArray events) throws JSONException{
		 List<Item> itemList = new ArrayList<>();
		 for(int i = 0; i < events.length(); i++) {
			 JSONObject obj = events.getJSONObject(i);
			 // static class
			 Item.ItemBuilder builder = new Item.ItemBuilder();
			 if(!obj.isNull(NAME)) {
				 builder.setName(obj.getString(NAME));
			 }
			 if(!obj.isNull(ID)) {
				 builder.setItemId(obj.getString(ID));
			 }
			 if(!obj.isNull(URL_STR)) {
				 builder.setUrl(obj.getString(URL_STR));
			 }
			 if(!obj.isNull(DISTANCE)) {
				 builder.setDistance(obj.getDouble(DISTANCE));
			 }
			 builder.setAddress(getAddress(obj));
			 builder.setCategories(getCategories(obj));
			 builder.setImageUrl(getImageURL(obj));
			 
			 itemList.add(builder.build());
		 }
		 return itemList;
	 }
	 
	 private String getAddress(JSONObject event) throws JSONException{
		 if(!event.isNull(EMBEDDED)) {
			 JSONObject embedded = event.getJSONObject(EMBEDDED);
			 if(!embedded.isNull(VENUES)) {
				 JSONArray venues = embedded.getJSONArray(VENUES);
				 for(int i = 0; i < venues.length(); i++) {
					 JSONObject venue = venues.getJSONObject(i);
					 StringBuilder sb = new StringBuilder();
					 if(!venue.isNull(ADDRESS)) {
						 JSONObject address = venue.getJSONObject(ADDRESS);
						 if(!address.isNull(LINE1)) {
							 sb.append(address.get(LINE1)); 
						 }
						 if(!address.isNull(LINE2)) {
							 sb.append(address.get(LINE2));
						 }
						 if(!address.isNull(LINE3)) {
							 sb.append(address.get(LINE3));
						 }
					 }
					 if(!venue.isNull(CITY)) {
						 JSONObject city = venue.getJSONObject(CITY);
						 if(!city.isNull(NAME)) {
							 sb.append(city.getString(NAME));
						 }
					 }
					 String addr = sb.toString();
					 if (!addr.equals("")) {
						return addr;
					}
				 }
			 }
		 }
		 return "";
	 }
	 
	 private Set<String> getCategories(JSONObject event) throws JSONException {
			Set<String> categories = new HashSet<>();

			if (!event.isNull(CLASSIFICATIONS)) {
				JSONArray classifications = event.getJSONArray(CLASSIFICATIONS);
				
				for (int i = 0; i < classifications.length(); ++i) {
					JSONObject classification = classifications.getJSONObject(i);
					
					if (!classification.isNull(SEGMENT)) {
						JSONObject segment = classification.getJSONObject(SEGMENT);
						
						if (!segment.isNull(NAME)) {
							categories.add(segment.getString(NAME));
						}
					}
				}
			}

			return categories;
	}
	 private String getImageURL(JSONObject event) throws JSONException{
		 if(!event.isNull(IMAGES)) {
			 JSONArray array = event.getJSONArray(IMAGES);
			 for(int i = 0; i < array.length(); i++) {
				 JSONObject image = array.getJSONObject(i);
				 if(!image.isNull(URL_STR)) {
					 return image.getString(URL_STR);
				 }
			 }
		 }
		 return "";
	 }
	 
	 
	 
// debug
	 private void queryAPI(double lat, double lon) {
		 List<Item> itemList = search(lat, lon, null);
			try {
				for (Item item : itemList) {
					JSONObject jsonObject = item.toJSONObject();
					System.out.println(jsonObject);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

	}
	 /**
		 * Main entry for sample TicketMaster API requests.
		 */
		public static void main(String[] args) {
			TicketMasterAPI tmApi = new TicketMasterAPI();
			// Mountain View, CA
			// tmApi.queryAPI(37.38, -122.08);
			// London, UK
			// tmApi.queryAPI(51.503364, -0.12);
			// Houston, TX
			tmApi.queryAPI(29.682684, -95.295410);
		}

}
