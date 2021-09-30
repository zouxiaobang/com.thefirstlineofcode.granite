package com.thefirstlineofcode.granite.lite.xeps.msgoffline;

import org.pf4j.Extension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.thefirstlineofcode.granite.framework.adf.spring.ISpringConfiguration;

@Extension
@Configuration
@ComponentScan
public class OfflineMessageConfiguration implements ISpringConfiguration {}
