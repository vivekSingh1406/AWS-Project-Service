package com.example.project.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private int id;
    private String name;
    private String email;
    private String description;
    private int age;
}
