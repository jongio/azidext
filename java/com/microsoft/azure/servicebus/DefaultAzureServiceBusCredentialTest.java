package com.microsoft.azure.servicebus;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.security.DefaultAzureServiceBusCredential;
import org.junit.Test;

public class DefaultAzureServiceBusCredentialTest {

    @Test
    public void testDefaultCredential() throws ServiceBusException, InterruptedException {
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();
        TopicClient topicClient = new TopicClient("jongsb2", "srnagartopic",
            new ClientSettings(new DefaultAzureServiceBusCredential()));

        topicClient.send(new Message("hello"));
    }
}
