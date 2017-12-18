package com.mcg.batch.test.core;

import com.mcg.batch.utils.PropertiesConfiguration;

public class TestProperties {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	System.setProperty("watt.scb.custom.properties.file", "C:\\Users\\ANAB\\Desktop\\utilities\\jmsutility.properties;C:\\Users\\ANAB\\Desktop\\utilities\\jmxUtility.properties");
	System.out.println("TestProperties.main()" + PropertiesConfiguration.getPropertiesString());
    }
}
