package com.kq.core.exceptions;

public class CircularDependencyException extends DIException{
    public CircularDependencyException(String msg){
        super(msg);
    }
}
