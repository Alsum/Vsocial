package com.src.vsocial;

public class Tweet {

	String author;
	String content;
	
	public Tweet(String author,String content) {
		this.author=author;
		this.content=content;
	}
	
	public Tweet() {
		// TODO Auto-generated constructor stub
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	
	
}
