package com.project.udacity.ratio.models;

/**
 * Created by mehseti on 30.7.2018.
 */

public class User {
    private String username;
    private String email;
    private String gender;
    private int age;
    private String id;

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public User(String username, String email, String gender, int age, String id) {
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.id = id;
    }
}
