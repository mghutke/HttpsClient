package com.httpsclient.HttpsClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@SpringBootApplication
@RestController
public class HttpsClientApplication {

	WebClient webClient;

	@PostConstruct
	public void init () throws IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException {

//		SslContext sslContext = SslContextBuilder
//				.forClient()
//				.trustManager(InsecureTrustManagerFactory.INSTANCE)
//				.build();
//		HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
//		 webClient = WebClient.builder()
//				 .baseUrl("https://localhost:8443/Message")
//				 .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//				 .clientConnector(new ReactorClientHttpConnector(httpClient)).build();

		//******************************* working code

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(new FileInputStream(ResourceUtils.getFile("classpath:httpsServer.p12")), "password".toCharArray());

		// Set up key manager factory to use our key store

		keyManagerFactory.init(keyStore, "password".toCharArray());

		// truststore
		KeyStore trustStore = KeyStore.getInstance("PKCS12");
		trustStore.load(new FileInputStream((ResourceUtils.getFile("classpath:httpsServer.p12"))), "password".toCharArray());


		trustManagerFactory.init(trustStore);

		SslContext sslContext = SslContextBuilder
				.forClient()
				.keyManager(keyManagerFactory)
				.trustManager(trustManagerFactory)
				.build();

		HttpClient httpClient = HttpClient.create().secure(sslSpec -> sslSpec.sslContext(sslContext));
		 webClient = WebClient.builder()
				.baseUrl("https://localhost:8443/Message")
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();



	}

	@GetMapping("/Hello")
	private Mono<String> getTheGreetingString(){

		return  webClient.get()
				.retrieve()
				.bodyToMono(String.class);


	}

	public static void main(String[] args) {
		SpringApplication.run(HttpsClientApplication.class, args);
	}

}
