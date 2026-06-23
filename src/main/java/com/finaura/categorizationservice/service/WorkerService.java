package com.finaura.categorizationservice.service;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finaura.categorizationservice.model.Category;
import com.finaura.categorizationservice.model.Expense;
import com.finaura.categorizationservice.model.ExpenseJob;
import com.finaura.categorizationservice.repository.CategoryRepository;
import com.finaura.categorizationservice.repository.ExpenseRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class WorkerService {
    
    private final RedisConfig redisConfig;
    private final GroqService groqService;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String buildLookupKey(String item, String merchant) {
        return item.trim().toLowerCase() + "|" + (merchant == null ? "" : merchant.trim().toLowerCase());
    }

    public WorkerService(RedisConfig redisConfig, GroqService groqService, CategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
        this.redisConfig = redisConfig;
        this.groqService = groqService;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @PostConstruct
    public void startWorker() {
        Thread workerThread = new Thread(() -> {
            try {
                Jedis jedis = redisConfig.createClient();
                System.out.println("Worker waiting for jobs...");
                
                while (true) {
                    List<String> result = jedis.blpop(0, "expense-category-jobs");
                    String json = result.get(1);

                    ExpenseJob job = objectMapper.readValue(json, ExpenseJob.class);
                    System.out.println("Received Job: " + job);

                    String category = groqService.categorizeExpense(job.getItem(), job.getMerchant(), job.getNotes());
                    System.out.println("Predicted Category: " + category);

                    String lookupKey = buildLookupKey(job.getItem(), job.getMerchant());

                    Category categoryDoc = new Category();
                    categoryDoc.setLookupKey(lookupKey);
                    categoryDoc.setCategory(category);

                    categoryRepository.save(categoryDoc);

                    Expense expense = expenseRepository.findById(job.getExpenseId()).orElse(null);
                    if(expense != null) {
                        expense.setCategory(category);
                        expense.setStatus("COMPLETED");
                        expenseRepository.save(expense);

                        System.out.println("Expense updated: " + expense.getId());
                    }
                    System.out.println("Category saved: " + category);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }   
        });
        workerThread.setDaemon(true);
        workerThread.start();
    }
}