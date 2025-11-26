package com.example.modules.monitoring.factory;

public class EnvironmentFactoryProducer {

    public static SystemEnvironmentFactory getFactory() {
        return new WindowsSystemFactory();
    }
}

