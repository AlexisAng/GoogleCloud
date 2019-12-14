package com.everis.storage.controller;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.everis.storage.GoogleStorageApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GoogleStorageApplication.class)
@WebAppConfiguration
public class FluxStorageControllerTest {

	private MockMvc mvc; //simula request y response
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@Before
	public void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build(); 
	}
	
	@Test
	public void testImagen() throws Exception {
		
		File file = new File("C:\\Users\\banguloa\\Pictures\\IBM\\whats.jpeg");
		String inputString = "{jsonCompare":"Google}";
	}

