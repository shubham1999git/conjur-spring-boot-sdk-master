package com.cyberark.conjur.springboot.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;
import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.google.gson.Gson;

/**
 * 
 * This custom class resolves the secret value at application load time from the
 * conjur vault.
 *
 */

public class CustomPropertySourceChain extends PropertyProcessorChain {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomPropertySourceChain.class);

	private PropertyProcessorChain chain;

	private SecretsApi secretsApi;
	

	public CustomPropertySourceChain(String name) {
		super("customPropertySource");
	
			LOGGER.debug("Calling CustomPropertysource Chain ");
		
	}

	@Override
	public void setNextChain(PropertyProcessorChain nextChain) {
		this.chain = nextChain;

	}

	public void setSecretsApi(SecretsApi secretsApi) {
		this.secretsApi = secretsApi;
	}

	@Override
	public String[] getPropertyNames() {
		LOGGER.info("CustomPropertySourceChain getPropertyNames method");
		return new String[0];
	}

	@Override
	public Object getProperty(String key) {

		//byte[] result = null;
		StringBuilder kind = new StringBuilder();
		Object secretValue = null;
		key = ConjurConfig.getInstance().mapProperty(key);
		if (!(key.startsWith(ConjurConstant.SPRING_VAR)) && !(key.startsWith(ConjurConstant.SERVER_VAR))
				&& !(key.startsWith(ConjurConstant.ERROR)) && !(key.startsWith(ConjurConstant.SPRING_UTIL))
				&& !(key.startsWith(ConjurConstant.CONJUR_PREFIX)) && !(key.startsWith(ConjurConstant.ACTUATOR_PREFIX))
				&& !(key.startsWith(ConjurConstant.LOGGING_PREFIX)) && !(key.startsWith(ConjurConstant.KUBERNETES_PREFIX))) {
//				logger.info("Inside getProperty CustomPropertySourceChain if loop ");
				String account = ConjurConnectionManager.getAccount(secretsApi);
//				logger.info("Inside getProperty account "+account);
//				String secretValue = secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, key);
//				logger.info("Inside getProperty secretValue "+secretValue);
//				result = secretValue != null ? secretValue.getBytes() : null;
				
				LOGGER.info("**** Getting secrets for account: {}***** ");
				//String[] keys = key.split(",");
				if(key.contains(",")) {
				String[] keys = key.split(",");
				for (int i = 0; i <= keys.length; i++) {
					if (i < keys.length - 1) {
						//kind.append("myConjurAccount:variable:" + keys[i] + ",");
						kind.append(account + ":variable:" + keys[i] + ",");
						LOGGER.info("*** inside getProperty keys if ***"+ keys[i]+i);
					} else if (i == keys.length - 1) {
						kind.append(account + ":variable:" + keys[i] + "");
						LOGGER.info("*** inside getProperty keys else ***"+ keys[i]+i);
					}
				}
				try {
					LOGGER.info("*** Before calling  getSecrets *** "+new String(kind));
					
				    Gson gson = new Gson();
					secretValue = secretsApi.getSecrets(new String(kind));
					Object valObject = gson.toJson(secretValue, Object.class);
					LOGGER.info("*** After calling  getSecrets of result *** "+valObject.toString());
					secretValue = valObject;
					
				} catch (ApiException e) {
					LOGGER.error("Status code: " + e.getCode());
					LOGGER.error("Reason: " + e.getResponseBody());
					LOGGER.error(e.getMessage());
				}
				
				}
				else {
					try {
					 secretValue = secretsApi.getSecret(account, ConjurConstant.CONJUR_KIND, key);
					 
					 LOGGER.info("*** After calling  getSecrets of result else *** "+secretValue.toString());
					} catch (ApiException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//result = secretValue != null ? secretValue.getBytes() : null;	
				
			}
		return secretValue;
	}
	
//	private byte[] processMultipleSecretResult(Object result) {
//		Map<String, String> map = new HashMap<String, String>();
//		String[] parts = result.toString().split(",");
//		for(String val : parts)
//		LOGGER.info("processMultipleSecretResult val " +val);
//		{
//			for (int j = 0; j < parts.length; j++) {
//				String[] splitted = parts[j].split("[:/=]");
//				for(String str:splitted){
//				}
//				for (int i = 0; i < splitted.length; i++) {
//					map.put(splitted[3], splitted[4]);
//				}
//			}
//		}
//		LOGGER.info("Final processMultipleSecretResult "+map.toString().getBytes());
//		return map.toString().getBytes();
//	}
}
