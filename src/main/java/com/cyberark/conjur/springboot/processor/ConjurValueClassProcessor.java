
package com.cyberark.conjur.springboot.processor;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import com.cyberark.conjur.springboot.annotations.ConjurValue;
import com.cyberark.conjur.springboot.annotations.ConjurValues;
/**
 * 
 * Annotation ConjurValues class processor.
 *
 */
public class ConjurValueClassProcessor implements BeanPostProcessor {

	private final ConjurRetrieveSecretService conjurRetrieveSecretService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurValueClassProcessor.class);

	public ConjurValueClassProcessor(ConjurRetrieveSecretService conjurRetrieveSecretService) {
		this.conjurRetrieveSecretService = conjurRetrieveSecretService;
	}


	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		Class<?> managedBeanClass = bean.getClass();

		List<Field> fieldList = FieldUtils.getAllFieldsList(managedBeanClass);
		
		//LOGGER.info("postProcessBeforeInitialization method*****");

		for (Field field : fieldList) {
			if (field.isAnnotationPresent(ConjurValue.class)) {
				ReflectionUtils.makeAccessible(field);
				String variableId = field.getDeclaredAnnotation(ConjurValue.class).key();
				byte[] result;
				try {
					LOGGER.info("postProcessBeforeInitialization *****");
					result = conjurRetrieveSecretService.retriveSingleSecretForCustomAnnotation(variableId);
					LOGGER.info("After calling retriveSingleSecretForCustomAnnotation *****");
					field.set(bean, result);
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				}
			}
			else if (field.isAnnotationPresent(ConjurValues.class)) {
				LOGGER.info("postProcessBeforeInitialization ConjurValues loop *****");
				ReflectionUtils.makeAccessible(field);
				String[] variableId = field.getDeclaredAnnotation(ConjurValues.class).keys();
				byte[] result;
				try {
					LOGGER.info("After calling retriveMultipleSecretsForCustomAnnotation *****"+variableId);
					result = conjurRetrieveSecretService.retriveMultipleSecretsForCustomAnnotation(variableId);
					LOGGER.info("After calling retriveMultipleSecretsForCustomAnnotation *****");
					field.set(bean, result);

				} catch (Exception e1) {
					LOGGER.error(e1.getMessage());
				}
			}
			else {
				//LOGGER.info("postProcessBeforeInitialization else *****");
			}

		}

		return bean;
	}

	@Nullable
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
