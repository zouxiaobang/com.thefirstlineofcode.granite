package com.firstlinecode.granite.cluster.node.mgtnode;

import java.util.Map;

import com.firstlinecode.granite.cluster.node.commons.options.AbstractOptionsTool;
import com.firstlinecode.granite.cluster.node.commons.options.OptionRule;

public class OptionsTool extends AbstractOptionsTool<Options> {

	public OptionsTool() {
		super("mgtnode.ini");
	}

	@Override
	protected Map<String, OptionRule> buildOptionRules(Map<String, OptionRule> optionRules) {
		optionRules.put("http-port",
				new OptionRule().
					setRange(OptionRule.Range.BOTH).
					setDataType(OptionRule.DataType.INTEGER));
		
		optionRules.put("repository-dir",
				new OptionRule().
					setRange(OptionRule.Range.BOTH).
					setDataType(OptionRule.DataType.STRING));
		
		optionRules.put("repack",
				new OptionRule().
					setRange(OptionRule.Range.BOTH).
					setDataType(OptionRule.DataType.BOOLEAN));
		
		return optionRules;
	}

	@Override
	protected Options createOptions() {
		return new Options();
	}

	@Override
	protected void printUsage() {
		System.out.println("Usage: java com.firstlinecode.granite.cluster.mgtnode.Main [OPTIONS]");
		System.out.println("OPTIONS:");
		System.out.println("--help                              Display help information.");
		System.out.println("--http-port=HTTP_PORT               Specify the http port that mgt node to use.");
		System.out.println("--config-dir=CONFIG_DIR             Specify the path of configuration directory.");
		System.out.println("--repository-dir=REPOSITORY_DIR     Specify the path of repository directory.");
	}

}
