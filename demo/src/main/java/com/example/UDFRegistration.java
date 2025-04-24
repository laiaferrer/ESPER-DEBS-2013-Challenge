package com.example;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

public class UDFRegistration {
    public static EPServiceProvider registerUDF() {
        // Create configuration for Esper engine
        Configuration configuration = new Configuration();
        
        // Register the UDF class with Esper
        configuration.addImport("com.example.ClosestPlayerUtils.*");
        configuration.addImport(ClosestPlayerUtils.class.getName());

        // Create EPServiceProvider instance
        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(configuration);


        // Explicitly register the function as a single-row UDF
        configuration.addPlugInSingleRowFunction("getDistance", "com.example.ClosestPlayerUtils", "getDistance");
        
        
        
        return epService;
    }

    public static void main(String[] args) {
        EPServiceProvider epService = registerUDF();
        System.out.println("UDF registered successfully.");

    }
}
