package com.thefirstlineofcode.granite.cluster.pipeline;

public class Constants {
	private Constants() {}
	
	public static final String COMPONENT_ID_IGNITE = "cluster.ignite";
	public static final String DEPENDENCY_ID_NODE_RUNTIME_CONFIGURATION = "node.runtime.configuration";
	public static final String COMPONENT_ID_CLUSTER_NODE_RUNTIME_CONFIGURATION = "cluster.node.runtime.configuration";
	
	public static final String COMPONENT_ID_STREAM_2_PARSING_MESSAGE_CONNECTOR = "stream.2.parsing.message.receiver";
	public static final String COMPONENT_ID_PARSING_2_PROCESSING_MESSAGE_CONNECTOR = "parsing.2.processing.message.receiver";
	public static final String COMPONENT_ID_ANY_2_EVENT_MESSAGE_CONNECTOR = "any.2.event.message.receiver";
	public static final String COMPONENT_ID_ANY_2_ROUTING_MESSAGE_CONNECTOR = "any.2.routing.message.receiver";
}
