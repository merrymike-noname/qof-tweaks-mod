package com.kovalenko.qoltweaks.core;

public interface Feature {
    String getId();
    boolean isEnabled();
    void register();
}