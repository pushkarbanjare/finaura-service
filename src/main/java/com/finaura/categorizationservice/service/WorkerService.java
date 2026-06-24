package com.finaura.categorizationservice.service;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finaura.categorizationservice.model.Category;
import com.finaura.categorizationservice.model.ExpenseJob;
import com.finaura.categorizationservice.repository.CategoryRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class WorkerService {
    
    private final RedisConfig redisConfig;
    private final GroqService groqService;
    private final CategoryRepository categoryRepository;
    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String buildLookupKey(String item, String merchant) {
        return item.trim().toLowerCase() + "|" + (merchant == null ? "" : merchant.trim().toLowerCase());
    }

    public WorkerService(RedisConfig redisConfig, GroqService groqService, CategoryRepository categoryRepository, MongoTemplate mongoTemplate) {
        this.redisConfig = redisConfig;
        this.groqService = groqService;
        this.categoryRepository = categoryRepository;
        this.mongoTemplate = mongoTemplate;
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

                    Category existingCategory = categoryRepository.findByLookupKey(lookupKey);
                    if (existingCategory == null) {
                        Category categoryDoc = new Category();
                        categoryDoc.setLookupKey(lookupKey);
                        categoryDoc.setCategory(category);
                        categoryRepository.save(categoryDoc);
                        System.out.println("Category saved: " + category);
                    }
                    else System.out.println("Category Already Exists");

                    Query query = new Query(Criteria.where("_id").is(job.getExpenseId()));
                    Update update = new Update().set("category", category).set("status", "COMPLETED");
                    UpdateResult updateResult = mongoTemplate.updateFirst(query, update, "expenses");

                    System.out.println("Matched Count: " + updateResult.getMatchedCount());
                    System.out.println("Modified Count: " + updateResult.getModifiedCount());

                    if (updateResult.getMatchedCount() == 0) System.out.println("Expense NOT FOUND: " + job.getExpenseId());
                    else System.out.println("Expense Updated: " + job.getExpenseId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }   
        });
        workerThread.setDaemon(true);
        workerThread.start();
    }
}