package com.everis.storage.model;

import org.springframework.stereotype.Component;

@Component
public class Features {
	
	private String type = new String();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	

}
