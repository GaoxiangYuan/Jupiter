package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import db.mysql.MySQLConnection;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();
		
		// Step 1, get all favorited itemids
		DBConnection conn = DBConnectionFactory.getConnection();
		Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
		// Step 2, get all categories, sort by count
		Map<String, Integer> allCategories = new HashMap<>();
		for(String itemId: favoriteItemIds) {
			Set<String> categories = conn.getCategories(itemId);
			for(String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) ->{
			return Integer.compare(e2.getValue(), e1.getValue());
		});
		// Step 3, search based on category, filter out favorite items
		Set<String> visitedItemIds = new HashSet<>();
		for(Entry<String, Integer> category : categoryList) {
			List<Item> items = conn.searchItems(lat, lon, category.getKey());
			for(Item item : items) {
				if(!favoriteItemIds.contains(item.getItemId()) && ! visitedItemIds.contains(item.getItemId())){
					visitedItemIds.add(item.getItemId());
					recommendedItems.add(item);
				}

			}
			
		}
		conn.close();
		return recommendedItems;
  }

}
