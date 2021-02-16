package com.pos.olympia.model;

public class ModelStock {

    private String StockId;
    private int StockQuantity;
    private int StockPrice;
    private String Barcode;
    private String ItemId;
    private String ItemName;
    private String CreatedTime;

    public String getStockId() {
        return StockId;
    }

    public void setStockId(String stockId) {
        StockId = stockId;
    }

    public int getStockQuantity() {
        return StockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        StockQuantity = stockQuantity;
    }

    public int getStockPrice() {
        return StockPrice;
    }

    public void setStockPrice(int stockPrice) {
        StockPrice = stockPrice;
    }

    public String getItemId() {
        return ItemId;
    }

    public void setItemId(String itemId) {
        ItemId = itemId;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public String getCreatedTime() {
        return CreatedTime;
    }

    public void setCreatedTime(String createdTime) {
        CreatedTime = createdTime;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }
}
