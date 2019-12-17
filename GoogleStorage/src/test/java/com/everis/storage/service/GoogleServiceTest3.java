package com.everis.storage.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;



import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.everis.storage.model.ResponseFinal;
import com.google.cloud.storage.Blob;

import reactor.core.publisher.Mono;

class GoogleServiceTest3 {
  
	
	GoogleService googleService =  new GoogleService();
	
	ResponseFinal responsefinal = new ResponseFinal();
	
	@Test
	void testGoogleVision() throws IOException {
	
	        ClassLoader cl = getClass().getClassLoader();
           File file = new File(cl.getResource("./ImagenesTest/whats.jpeg").getFile());
     
	        Path path = file.toPath();
	       
	        String contentType = Files.probeContentType(path);
	       
	        byte[] bytesImagen = Files.readAllBytes(path);
	       
	        MultipartFile multipartFile = new MockMultipartFile("file",
	                file.getName(), contentType, bytesImagen);
	       
	        Mono<String> vision = googleService.googleVision(multipartFile);
	        String response = vision.block();
	        assertTrue(response.contains("description"));
	        assertEquals( "image/jpeg",contentType);

	}
	
	@Test
	void testGoogleStorage() throws IOException  {
		
		ClassLoader cl = getClass().getClassLoader();
        File file = new File(cl.getResource("./ImagenesTest/whats.jpeg").getFile());
        
        Path path = file.toPath();
        String contentType = Files.probeContentType(path);
	       
        byte[] bytesImagen = Files.readAllBytes(path);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), contentType, bytesImagen);
        
        String textInputJson = "{\"jsonCompare\":\"Google\"}";
        
        responsefinal = googleService.googleStorage(multipartFile, textInputJson);
        assertTrue(responsefinal.getIsSuccess());
        
		
	}

	@Test
	void testGoogleStorage1() throws IOException  {
		
		ClassLoader cl = getClass().getClassLoader();
        File file = new File(cl.getResource("./ImagenesTest/whats.jpeg").getFile());
        
        Path path = file.toPath();
        String contentType = Files.probeContentType(path);
	       
        byte[] bytesImagen = Files.readAllBytes(path);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), contentType, bytesImagen);
        
        String textInputJson = "{\"jsonCompare\":\"Ggfdsfdfdle\"}";
       
        responsefinal  =  googleService.googleStorage(multipartFile, textInputJson);
        assertEquals("La palabra escrita es MAYOR a la encontrada",responsefinal.getTextoEncontrado() );
        
		
	}
	
	@Test
	void testGoogleStorage2() throws IOException  {
		
		ClassLoader cl = getClass().getClassLoader();
        File file = new File(cl.getResource("./ImagenesTest/whats.jpeg").getFile());
        
        Path path = file.toPath();
        String contentType = Files.probeContentType(path);
	       
        byte[] bytesImagen = Files.readAllBytes(path);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), contentType, bytesImagen);
        
        String textInputJson = "{\"jsonCompare\":\"Goe\"}";
       
        responsefinal  =  googleService.googleStorage(multipartFile, textInputJson);
        assertEquals("La palabra es Menor a la encontrada",responsefinal.getTextoEncontrado() );
        
		
	}
	
	@Test
	void testBucket() throws IOException  {
		
		ClassLoader cl = getClass().getClassLoader();
        File file = new File(cl.getResource("./ImagenesTest/whats.jpeg").getFile());
        
        Path path = file.toPath();
        //String contentType = Files.probeContentType(path);
	       
        byte[] bytesImagen = Files.readAllBytes(path);
        
     
		
		String bucketName = "prueba_14547";
		Blob blob = googleService.validateBucket(bucketName).create("my-first-blob1", bytesImagen);
		
		
		String[] blobName = blob.getGeneratedId().split("/my-first-blob1");
        assertEquals(bucketName, blobName[0] );
		
	}
	
}
