package com.firstlinecode.granite.cluster.auth;

import org.bson.Document;

import com.firstlinecode.granite.cluster.dba.AbstractDbInitializer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

public class DbInitializer extends AbstractDbInitializer {

	@Override
	public void initialize(MongoDatabase database) {
		if (collectionExistsInDb(database, "users"))
			return;
		
		database.createCollection("users");
		MongoCollection<Document> users = database.getCollection("users");
		users.createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));
	}

}
