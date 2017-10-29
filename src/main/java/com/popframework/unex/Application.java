package com.popframework.unex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
    	System.out.println("Inicio");
        SpringApplication.run(Application.class, args);
        System.out.println("Fin");
    }
}
