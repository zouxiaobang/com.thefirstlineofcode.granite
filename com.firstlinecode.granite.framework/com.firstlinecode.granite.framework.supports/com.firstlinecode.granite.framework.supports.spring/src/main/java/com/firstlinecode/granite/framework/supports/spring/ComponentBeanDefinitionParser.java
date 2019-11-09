package com.firstlinecode.granite.framework.supports.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ComponentBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
	private static final String COMPONENT_ID = "component-id";
	private static final String COMPONENT_PROP = "componentId";
	private static final String REF = "ref";
	private static final String TARGET_BEAN_NAME_PROP = "targetBeanName";
	
	@Override
	protected Class<?> getBeanClass(Element element) {
		return SpringComponentFactoryBean.class;
	}
	
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		
		if (!element.hasAttribute(COMPONENT_ID)) {
			parserContext.getReaderContext().error(
					"granite:component must has a 'component-id' attribute.", element);
		}
		
		builder.addPropertyValue(COMPONENT_PROP, element.getAttribute(COMPONENT_ID));
		
		if (!element.hasAttribute(REF)) {
			parserContext.getReaderContext().error(
					"granite:component must has a 'ref' attribute.", element);
		}
		
		builder.addPropertyValue(TARGET_BEAN_NAME_PROP, element.getAttribute(REF));
	}
	
	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
}
