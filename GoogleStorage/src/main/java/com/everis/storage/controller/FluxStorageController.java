package com.everis.storage.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.everis.storage.service.GoogleService;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/storage")
public class FluxStorageController {

	@Autowired
	GoogleService service;
    
	@PostMapping("/imagen")
	public Mono<Object> imagen(@RequestParam("file") MultipartFile file,
			@RequestPart("JsonCompare") String jsonCompare) throws IOException {
		Flux<Object> flujo = Flux.merge(Mono.just(service.googleStorage(file, jsonCompare)),service.googleVision(file));
       
		Mono<List<Object>> salida = flujo.collectList();
        return salida.map(x -> x.get(0));
	}

}
