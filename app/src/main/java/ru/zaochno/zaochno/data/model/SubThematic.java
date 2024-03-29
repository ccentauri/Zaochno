package ru.zaochno.zaochno.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SubThematic implements Serializable {
    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private Integer id;

    public SubThematic(String name) {
        this.name = name;
    }

    public SubThematic(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }
}
