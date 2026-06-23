package com.finaura.categorizationservice.repository;

import com.finaura.categorizationservice.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExpenseRepository extends MongoRepository<Expense, String> {}