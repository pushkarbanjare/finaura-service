package com.finaura.categorizationservice.repository;

import com.finaura.categorizationservice.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Category findByLookupKey(String lookupKey);
}