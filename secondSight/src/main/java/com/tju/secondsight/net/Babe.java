package com.tju.secondsight.net;

public class Babe{
	private String url;
	private String title;
	
	public Babe(String url, String title){
		this.url = url;
		this.title = title;
	}
	
	public String getUrl(){
		return url;
	}
	public String getTitle(){
		return title;
	}
}