package com.finaura.categorizationservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;
    private String category;
    private String status;

    public String getId() {return id;}
    public String getCategory() {return category;}
    public String getStatus() {return status;}
    
    public void setId(String id) {this.id = id;}
    public void setCategory(String category) {this.category = category;}
    public void setStatus(String status) {this.status = status;}
}