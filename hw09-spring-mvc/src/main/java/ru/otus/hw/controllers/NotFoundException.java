package ru.otus.hw.controllers;

public class NotFoundException extends RuntimeException{

    NotFoundException() {
        super("Person not found");
    }
}
