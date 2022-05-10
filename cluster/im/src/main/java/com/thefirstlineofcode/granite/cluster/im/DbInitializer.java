package com.thefirstlineofcode.granite.cluster.im;

import org.bson.Document;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.thefirstlineofcode.granite.cluster.dba.AbstractDbInitializer;

public class DbInitializer extends AbstractDbInitializer {

	@Override
	public void initialize(MongoDatabase database) {
		if (collectionExistsInDb(database, "subscriptions"))
			return;
		
		database.createCollection("subscriptions");
		MongoCollection<Document> subscriptions = database.getCollection("subscriptions");
		subscriptions.createIndex(Indexes.compoundIndex(Indexes.ascending("user"), Indexes.ascending("contact")), new IndexOptions().unique(true));
		
		database.createCollection("subscription_notifications");
		MongoCollection<Document> subscriptionNofications = database.getCollection("subscription_notifications");
		subscriptionNofications.createIndex(Indexes.ascending("user"));
	}

}
