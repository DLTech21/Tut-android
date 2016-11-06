package com.dtalk.dd.model;

import java.io.Serializable;

public class Photo4Gallery implements Serializable {

	public final static int FUNCTION_TYPE = 0;
	public final static int NORMAL_YPE = 1;
	
	public String path;
	public int type;
	
	public Photo4Gallery() {
		
	}
	
	public Photo4Gallery(int type) {
		this.type = type;
	}
	
	public Photo4Gallery(int type, String path) {
		this.type = type;
		this.path = path;
	}
}
