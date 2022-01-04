package com.navercorp.newbie.todolist.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ObjectData {
    private byte[] file;
    private String contentType;
}
