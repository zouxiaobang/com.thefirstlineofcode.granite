package com.thefirstlineofcode.granite.lite.im;

import org.pf4j.Extension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.thefirstlineofcode.granite.framework.adf.spring.ISpringConfiguration;

@Extension
@Configuration
@ComponentScan
public class ImConfiguration implements ISpringConfiguration {}
