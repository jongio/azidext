package com.jongallant.azure.identity.extensions;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.jongallant.azure.identity.extensions.DefaultAzureServiceBusCredential;
import org.junit.Test;

public class DefaultAzureServiceBusCredentialTest {

    @Test
    public void testDefaultCredential() throws ServiceBusException, InterruptedException {
        TopicClient topicClient = new TopicClient("jongsb2", "srnagartopic",
            new ClientSettings(new DefaultAzureServiceBusCredential()));
        topicClient.send(new Message("hello"));
        topicClient.close();
    }
}
