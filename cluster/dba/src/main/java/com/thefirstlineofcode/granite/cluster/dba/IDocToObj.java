package com.thefirstlineofcode.granite.cluster.dba;

import org.bson.Document;

public interface IDocToObj<T> {
	T toObj(Document doc);
}
