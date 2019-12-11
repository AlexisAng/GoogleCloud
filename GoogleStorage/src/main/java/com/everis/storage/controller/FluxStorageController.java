package com.everis.storage.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.everis.storage.model.ResponseFinal;
import com.everis.storage.service.GoogleService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/storage")
public class FluxStorageController {

	@Autowired
	GoogleService service;

	@RequestMapping(value = "/imagen", method = RequestMethod.POST)
	public ResponseFinal imagen(@RequestParam("file") MultipartFile file,
			@RequestPart("JsonCompare") String JsonCompare) throws IOException {

		// @RequestParam String JsonObject
		byte[] fileContent = file.getBytes();
		String JsonCompare1 = JsonCompare;

		Mono<String> mono1 = service.googleVision(fileContent);
		ResponseFinal mono2 = service.googleStorage(fileContent, JsonCompare1);

		Flux<Object> flujo = Flux.merge(Mono.just(mono2), mono1);

		return mono2;
	}

}
