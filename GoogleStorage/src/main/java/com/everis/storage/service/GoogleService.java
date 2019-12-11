package com.everis.storage.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;

import reactor.core.publisher.Mono;

@Service
public class GoogleService {

	@Value("${Credenciales.google}")
    private String credencial;
	
	final String API_BASE_URL = "https://vision.googleapis.com";
    //final String API_BASE_URL = "https://webhook.site/08c7ce57-7ba5-454a-9f14-035d9f058953";
    final String API_URI = "/v1p4beta1/images:annotate";
    final String API_TEST = "https://webhook.site/08c7ce57-7ba5-454a-9f14-035d9f058953";
    
	public Mono<String> imagen (JSonDao body ) {
		WebClient.Builder builder = WebClient.builder().baseUrl(API_BASE_URL).defaultHeader(HttpHeaders.CONTENT_TYPE,
				MediaType.APPLICATION_JSON_VALUE);

		WebClient webClient = builder.build();

		Mono<String> response = webClient.post()
				.uri("/v1p4beta1/images:annotate?key={apikey}", "AIzaSyDr5r_gSonvm1klOoKxkANo-2w2pRVLNHQ")
				.body(BodyInserters.fromObject(body)).exchange().flatMap(x -> {
					if (!x.statusCode().is2xxSuccessful())
						return Mono.just(API_BASE_URL + " Called. Error 4xx: " + x.statusCode() + "\n");
					return x.bodyToMono(String.class);
				});

		
		return response;
		
		

	}
    public Mono<String> googleVision(byte[] imagenString)
	{
    	
		JSonDao solicitud = new JSonDao();
		
		String encodedString = Base64.getEncoder().encodeToString(imagenString);
		
		Image imagen = new Image();
		imagen.setContent(encodedString);
		
		String type = "TEXT_DETECTION";
		Features caracteristicas = new Features();
		caracteristicas.setType(type);

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
    
    
    public ResponseFinal googleStorage(byte[] imagenString, String JsonCompare) throws FileNotFoundException, IOException
	{
		
        
		Credentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credencial));
      
		
		Storage storage1 = StorageOptions.newBuilder().setCredentials(credentials)
              .build().getService();
		
		String bucketName = "prueba1_alexis";
		
		Storage storage2 = StorageOptions.getDefaultInstance().getService();
	
		    Bucket bucket =
		    	    storage1.create(
		    	        BucketInfo.newBuilder(bucketName)
		    	            // See here for possible values: http://g.co/cloud/storage/docs/storage-classes
		    	            .setStorageClass(StorageClass.COLDLINE)
		    	            // Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
		    	            .setLocation("asia")
		    	         // Modify access list to allow all users with link to read file
		    	              .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.OWNER))))
		    	            .build());

		    
		    Blob blob = bucket.create("my-first-blob", imagenString);
		    
		    Gson gson = new Gson();	
		    JsonCompareObject jsontext =  gson.fromJson(JsonCompare, JsonCompareObject.class);
		    
		    Mono<String> imagen =  googleVision(imagenString) ;
		    
		    String[]respuestaVision = getResponse(imagen).split("\"description\":");
		    String[] textoEncontrado = respuestaVision[1].split(",");
		    
		    Boolean found = textoEncontrado[0].contains(jsontext.getJsonCompare());
		    
		    String ruta = blob.getMediaLink();
		    
		   
		   
		    //Response final
            ResponseFinal responsefinal = new ResponseFinal();
		    
		    responsefinal.setTextoRequerido(jsontext.getJsonCompare());
		    responsefinal.setRutaImagen(ruta);
		    
		    
		    if (found) {
		    	responsefinal.setTextoEncontrado(textoEncontrado[0]);
		    	responsefinal.setIsSuccess(true);
		    	Gson gson1 = new Gson();
				// Convert java object to Json
				String salidafinal = gson1.toJson(responsefinal);
				return responsefinal;
		    }
		    	
		    else {
		    responsefinal.setTextoEncontrado(textoEncontrado[0]);
		    responsefinal.setTextoEncontrado(textoEncontrado[0]);
			responsefinal.setIsSuccess(false);
			//return Mono.just("Texto No encontrado");
			return responsefinal;
		    
		    }
		    
		   
	}
    
    public String getResponse (Mono<String> mono) {
    return mono.block();
    }
}