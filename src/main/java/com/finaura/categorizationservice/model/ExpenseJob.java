package com.finaura.categorizationservice.model;

public class ExpenseJob {
    
    private String expenseId;
    private String item;
    private String merchant;
    private String notes;

    public ExpenseJob() {}
    
    public String getExpenseId() {return expenseId;}
    public void setExpenseId(String expenseId) {this.expenseId = expenseId;}

    public String getItem() {return item;}
    public void setItem(String item) {this.item = item;}

    public String getMerchant() {return merchant;}
    public void setMerchant(String merchant) {this.merchant = merchant;}

    public String getNotes() {return notes;}
    public void setNotes(String notes) {this.notes = notes;}

    @Override
    public String toString() {
        return "ExpenseJob{" +
            "expenseId='" + expenseId + '\'' +
            ", item='" + item + '\'' +
            ", merchant='" + merchant + '\'' +
            ", notes='" + notes + '\'' +
            '}';
    }
}