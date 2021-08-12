package com.firstlinecode.granite.lite.xeps.muc;

import org.pf4j.Extension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.firstlinecode.granite.framework.adf.spring.ISpringConfiguration;

@Extension
@Configuration
@ComponentScan
public class MucConfiguration implements ISpringConfiguration {}
