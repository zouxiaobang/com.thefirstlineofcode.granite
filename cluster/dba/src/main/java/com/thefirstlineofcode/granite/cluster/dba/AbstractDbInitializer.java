package com.thefirstlineofcode.granite.cluster.dba;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public abstract class AbstractDbInitializer implements IDbInitializer {

	protected boolean collectionExistsInDb(MongoDatabase database, String collectionName) {
		MongoCursor<String> cursor = database.listCollectionNames().iterator();
		while (cursor.hasNext()) {
			if (collectionName.equals(cursor.next()))
				return true;
		}
		
		return false;
	}
	
}
