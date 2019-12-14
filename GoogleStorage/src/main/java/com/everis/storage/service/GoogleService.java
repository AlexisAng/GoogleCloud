package com.everis.storage.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BucketGetOption;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;

import reactor.core.publisher.Mono;

@Service
public class GoogleService {
  
	
	private static Logger log = LoggerFactory.getLogger(GoogleService.class);
	Properties prop = new Properties();

	public Mono<String> imagen(JSonDao body) throws IOException {

		InputStream input = GoogleService.class.getClassLoader().getResourceAsStream("config.properties");
		if (input == null) {
			log.info("No se encontraron las propiedades");
		} else {
			log.info("Propiedades encontradas");
		}

		prop.load(input);
		String key = prop.getProperty("Key.Vision");
		String apiUrl = prop.getProperty("API.BASE.URL");
		String apiUri = prop.getProperty("Api.Uri");

		WebClient.Builder builder = WebClient.builder().baseUrl(apiUrl).defaultHeader(HttpHeaders.CONTENT_TYPE,
				MediaType.APPLICATION_JSON_VALUE);

		WebClient webClient = builder.build();

		
		return webClient.post().uri("/v1p4beta1/images:annotate?key={apikey}", key)
				.body(BodyInserters.fromValue(body)).exchange().flatMap(x -> {
					if (!x.statusCode().is2xxSuccessful())
						return Mono.just(apiUri + " Called. Error 4xx: " + x.statusCode() + "\n");
					else {
						log.info("Termine VISION");
						return x.bodyToMono(String.class);
					}
				});
		

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

	public ResponseFinal googleStorage(MultipartFile file, String jsonCompare) throws IOException
		 {
		
		boolean imagenJsonInputValidado=validateJSonStringImageFile(file ,jsonCompare);
		ResponseFinal responsefinal = new ResponseFinal();
		
		if(imagenJsonInputValidado) {
		try
		{
		
		
		boolean imagenExtensionValidada = validateImage(file);
		
		if(imagenExtensionValidada) {
		byte[] fileContent = file.getBytes();
		
		String bucketName = "prueba_17";
		
		Blob blob = validateBucket(bucketName).create("my-first-blob1", fileContent);
		responsefinal.setRutaImagen(blob.getMediaLink());	    
		Gson gson = new Gson();
		JsonCompareObject jsontext = gson.fromJson(jsonCompare, JsonCompareObject.class);
		Mono<String> imagen = googleVision(file);

		String[] respuestaVision = getResponse(imagen).split("\"description\": \"");
		String[] textoEncontrado = respuestaVision[1].split("\"");
		Boolean found = textoEncontrado[0].contains(jsontext.getJsonCompare());

		int lenght2 = jsontext.getJsonCompare().length();
		int lenght = textoEncontrado[0].length() - 2;
		String limpieza = textoEncontrado[0].substring(0, lenght);

		responsefinal.setTextoRequerido(limpieza);
		
		responsefinal.setIsSuccess(found);

		if (lenght == lenght2) {
				responsefinal.setTextoEncontrado(jsontext.getJsonCompare());
			} else if (lenght > lenght2) {
				responsefinal.setTextoEncontrado("La palabra es Menor a la encontrada");
				responsefinal.setIsSuccess(false);
			} else {
				responsefinal.setTextoEncontrado("La palabra escrita es MAYOR a la encontrada");
				log.info("Extension no Admitida");
				responsefinal.setIsSuccess(false);
			}
		}else {
			log.info("Extension de Archivo No Valida");
		}
		
		}catch (IOException e) {
			log.info("Error falta un campo");
		}
		}
		return responsefinal;
		
}


	public String getResponse(Mono<String> mono) {
		return mono.block();
	}

	
	public boolean validateImage(MultipartFile file) {

		String fileName = file.getOriginalFilename().toUpperCase();
		return (fileName.endsWith(".JPG") || fileName.endsWith(".JPEG") || fileName.endsWith(".PNG"));
		
		
		
	}


	public Bucket validateBucket(String name) throws IOException {

		InputStream input = GoogleService.class.getClassLoader().getResourceAsStream("config.properties");
		if (input == null) {
			log.info("No se encontraron las propiedades");
		} else {
			log.info("Propiedades encontradas");
		}

		prop.load(input);
		String credenciales = prop.getProperty("Credencial.google");

		Credentials credentials = GoogleCredentials.fromStream(new FileInputStream(credenciales));
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		Bucket bucket = storage.get(name, BucketGetOption.fields(Storage.BucketField.values()));

		if (bucket == null) {

			  bucket = storage.create(BucketInfo.newBuilder(name) 
					
					.setStorageClass(StorageClass.COLDLINE)
					
					.setLocation("asia")
					
					.setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.OWNER)))).build());
			return bucket;

		} else {
		     bucket.toBuilder().setVersioningEnabled(true).build().update();
			return bucket;

		}

	}

	public boolean validateJSonStringImageFile (MultipartFile file , String jsonText) {

		return ( jsonText != null && !jsonText.contentEquals("") && !jsonText.isEmpty() &&
                file != null && !file.isEmpty() );
	}
	
}