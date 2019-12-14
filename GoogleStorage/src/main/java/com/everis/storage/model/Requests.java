package com.everis.storage.model;

import java.util.ArrayList;
import java.util.List;


public class Requests {

	Image image;
	
	List<Features>features = new ArrayList<>();

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public List<Features> getFeatures() {
		return features;
	}

	public void setFeatures(List<Features> features) {
		this.features = features;
	}

	


	

}
