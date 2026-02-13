package com.cm.sanchalak.dto.finance;

import java.math.BigDecimal;

public class FeeStructureItemDto {
    private Long id;
    private Long categoryId;
    private String categoryName; // For display
    private BigDecimal amount;

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public BigDecimal getAmount() { return amount; }

    public void setId(Long id) { this.id = id; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
