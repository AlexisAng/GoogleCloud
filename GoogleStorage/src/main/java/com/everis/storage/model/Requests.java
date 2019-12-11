package com.everis.storage.model;

import java.util.ArrayList;


public class Requests {

	Image image = new Image();
	
	ArrayList<Features>features = new ArrayList<Features>();

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public ArrayList<Features> getFeatures() {
		return features;
	}

	public void setFeatures(ArrayList<Features> features) {
		this.features = features;
	}

	

}
