package com.everis.storage.model;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class GoogleResponse {

	@JsonProperty("responses")
	public Map<String, Object> responses;
	
	
}
