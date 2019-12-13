package com.everis.storage.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.everis.storage.model.Features;
import com.everis.storage.model.Image;
import com.everis.storage.model.JSonDao;
import com.everis.storage.model.JsonCompareObject;
import com.everis.storage.model.Requests;
import com.everis.storage.model.ResponseFinal;
import com.google.api.client.util.Value;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Bucket.BucketSourceOption;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BucketGetOption;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import reactor.core.publisher.Mono;

@Service
public class GoogleService {

	Properties prop = new Properties();

	final String API_TEST = "https://webhook.site/08c7ce57-7ba5-454a-9f14-035d9f058953";

	public Mono<String> imagen(JSonDao body) throws IOException {

		InputStream input = GoogleService.class.getClassLoader().getResourceAsStream("config.properties");
		if (input == null) {
			System.out.println("No se encontraron las propiedades");
		} else {
			System.out.println("Propiedades encontradas");
		}

		prop.load(input);
		String key = prop.getProperty("Key.Vision");
		String API_URL = prop.getProperty("API.BASE.URL");
		String Api_Uri = prop.getProperty("Api.Uri");

		WebClient.Builder builder = WebClient.builder().baseUrl(API_URL).defaultHeader(HttpHeaders.CONTENT_TYPE,
				MediaType.APPLICATION_JSON_VALUE);

		WebClient webClient = builder.build();

		Mono<String> response = webClient.post().uri("/v1p4beta1/images:annotate?key={apikey}", key)
				.body(BodyInserters.fromObject(body)).exchange().flatMap(x -> {
					if (!x.statusCode().is2xxSuccessful())
						return Mono.just(Api_Uri + " Called. Error 4xx: " + x.statusCode() + "\n");
					else {
						System.out.println("Termine VISION");
						return x.bodyToMono(String.class);
					}
				});
		return response;

	}

	public Mono<String> googleVision(MultipartFile file) throws IOException {

		byte[] fileContent = file.getBytes();

		JSonDao solicitud = new JSonDao();

		String encodedString = Base64.getEncoder().encodeToString(fileContent);

		Image imagen = new Image();
		imagen.setContent(encodedString);

		Features caracteristicas = new Features();
		caracteristicas.setType("TEXT_DETECTION");

		Requests request = new Requests();
		ArrayList<Features> features = new ArrayList<>();
		features.add(caracteristicas);
		request.setFeatures(features);
		request.setImage(imagen);

		ArrayList<Requests> peticiones = new ArrayList<>();
		peticiones.add(request);
		solicitud.setRequests(peticiones);

		GoogleService servicio = new GoogleService();

		return servicio.imagen(solicitud);
	}

	public ResponseFinal googleStorage(MultipartFile file, String JsonCompare)
			throws FileNotFoundException, IOException {
		
		ResponseFinal responsefinal = new ResponseFinal();
		
		boolean imagenValidada = validateImage(file);
		
		if(imagenValidada==true) {
			
		byte[] fileContent = file.getBytes();
		String bucketName = "prueba_17";
		Blob blob = validateBucket(bucketName).create("my-first-blob1", fileContent);
	    
		boolean jsonValidado=validateJSonString(JsonCompare);
		
		
		Gson gson = new Gson();
		JsonCompareObject jsontext = gson.fromJson(JsonCompare, JsonCompareObject.class);

		Mono<String> imagen = googleVision(file);

		String[] respuestaVision = getResponse(imagen).split("\"description\": \"");
		String[] textoEncontrado = respuestaVision[1].split("\"");
		Boolean found = textoEncontrado[0].contains(jsontext.getJsonCompare());
	

		int lenght2 = jsontext.getJsonCompare().length();
		int lenght = textoEncontrado[0].length() - 2;
		String limpieza = textoEncontrado[0].substring(0, lenght);
		// Response final
		

		responsefinal.setTextoRequerido(limpieza);
		responsefinal.setRutaImagen(blob.getMediaLink());
		responsefinal.setIsSuccess(found);

		if (lenght == lenght2) {
			responsefinal.setTextoEncontrado(jsontext.getJsonCompare());
		} else if (lenght > lenght2) {
			responsefinal.setTextoEncontrado("La palabra es Menor a la encontrada");
			responsefinal.setIsSuccess(false);
		} else {
			responsefinal.setTextoEncontrado("La palabra escrita es MAYOR a la encontrada");
			responsefinal.setIsSuccess(false);
		}
		
		return responsefinal;
		}
		
		else
		{
			System.out.println("Extension no Admitida");
			return responsefinal;
		}
	}

	public String getResponse(Mono<String> mono) {
		return mono.block();
	}
	
	public boolean validateImage(MultipartFile file) {

	String fileName = file.getOriginalFilename().toUpperCase();
	boolean extension = fileName.endsWith(".JPG") || fileName.endsWith(".JPEG") || fileName.endsWith(".PNG");
	
	if (!extension) {
		return false;
		 
	   }else {
		   return true;
	   }
        }
	
	public Bucket validateBucket ( String name) throws IOException {
		

		InputStream input = GoogleService.class.getClassLoader().getResourceAsStream("config.properties");
		if (input == null) {
			System.out.println("No se encontraron las propiedades");
		} else {
			System.out.println("Propiedades encontradas");
		}

		prop.load(input);
		String credenciales = prop.getProperty("Credencial.google");
		
		Credentials credentials = GoogleCredentials.fromStream(new FileInputStream(credenciales));
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
		
		Bucket bucket = storage.get(name, BucketGetOption.fields(Storage.BucketField.values()));
		
		
		if (bucket==null) {
			
			Bucket bucket1 = storage.create(BucketInfo.newBuilder(name)
					// See here for possible values: http://g.co/cloud/storage/docs/storage-classes
					.setStorageClass(StorageClass.COLDLINE)
					// Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
					.setLocation("asia")
					// Modify access list to allow all users with link to read file
					.setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.OWNER)))).build());
			return bucket1;
		
		}
		else {
			Bucket updatedBucket = bucket.toBuilder().setVersioningEnabled(true).build().update();
		return updatedBucket;
			
		}
		
	
	}
	 
	public boolean validateJSonString (String JsonText) {
		return null != null;
	}
}