package com.example.not_used;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;



public class SimpleEsperExample {
    
    public class ProductEvent {
        private String productName;
        private double price;
    
        public ProductEvent(String productName, double price) {
            this.productName = productName;
            this.price = price;
        }
    
        public String getProductName() {
            return productName;
        }
    
        public double getPrice() {
            return price;
        }
    }

    public void run() {
        // Configure Esper  
        Configuration config = new Configuration();
        config.addEventType("ProductEvent", ProductEvent.class);
        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);

        // Define an EPL query to calculate the average price of products
        System.out.println("queryyyy");
        String expression = "select avg(price) as averagePrice from ProductEvent.win:time(5 sec)";
        EPStatement statement = epService.getEPAdministrator().createEPL(expression);
        System.out.println("EPL");

        MyListener listener = new MyListener();
        statement.addListener(listener);

        // Send sample events
        epService.getEPRuntime().sendEvent(new ProductEvent("Laptop", 800.00));
        System.out.println("laptop");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        epService.getEPRuntime().sendEvent(new ProductEvent("Phone", 500.00));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("phone");
        epService.getEPRuntime().sendEvent(new ProductEvent("Tablet", 300.00));
        
        // Keep the application running for some time to allow event processing
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("tablet");
    }

    public class MyListener implements UpdateListener {
        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            System.out.println("Listener triggered");
            if (newEvents != null && newEvents.length > 0) {
                EventBean event = newEvents[0];
                System.out.println("Average Price in last 30 seconds: " + event.get("averagePrice"));
            } else {
                System.out.println("No events received");
            }
        }
    }

    public static void main(String[] args) {
        SimpleEsperExample example = new SimpleEsperExample();
        System.out.println("run");
        example.run();
        System.out.println("run2");
    }
}
