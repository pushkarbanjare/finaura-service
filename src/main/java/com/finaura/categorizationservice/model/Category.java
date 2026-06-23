package com.finaura.categorizationservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
public class Category {

    @Id
    private String id;
    private String lookupKey;
    private String category;

    public String getId() {return id;}
    public String getLookupKey() {return lookupKey;}
    public String getCategory() {return category;}
    
    public void setId(String id) {this.id = id;}
    public void setLookupKey(String lookupKey) {this.lookupKey = lookupKey;}
    public void setCategory(String category) {this.category = category;}
}