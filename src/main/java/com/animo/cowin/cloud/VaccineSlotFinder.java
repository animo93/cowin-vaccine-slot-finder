package com.animo.cowin.cloud;

import java.util.Base64;
import java.util.logging.Logger;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

public class VaccineSlotFinder implements BackgroundFunction<PubSubMessage>{

	private static final Logger logger = Logger.getLogger(VaccineSlotFinder.class.getName());

	@Override
	public void accept(PubSubMessage message, Context context) throws Exception {
		String data = message.data != null
				? new String(Base64.getDecoder().decode(message.data))
						: "Hello, World";
		logger.info(data);

	}

}
