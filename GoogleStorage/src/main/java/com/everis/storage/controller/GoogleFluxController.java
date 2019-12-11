package com.everis.storage.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.everis.storage.service.GoogleService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/google")
public class GoogleFluxController {
	
//	@Autowired
//	GoogleService service;
//	
//
//	@RequestMapping(value = "/imagestorage", method = RequestMethod.POST)
//	public Flux<String> imagen(@RequestParam("file") MultipartFile file) throws IOException {
//		
//		//@RequestParam String JsonObject
//		byte[] fileContent = file.getBytes();
//
//		Mono<String> mono1 = service.googleVision(fileContent);
//		Mono<String> mono2 = service.googleStorage(fileContent);
//
//		Flux<String> flujo = Flux.merge(mono2, mono1);
//
//		return flujo;
//	}
//
}
