package com.atlantafx.features.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Product {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty price = new SimpleStringProperty();

    public Product(String name, String category, String price) {
        this.name.set(name);
        this.category.set(category);
        this.price.set(price);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public StringProperty priceProperty() {
        return price;
    }
}
