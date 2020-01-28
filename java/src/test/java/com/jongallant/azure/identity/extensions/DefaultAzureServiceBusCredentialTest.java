package com.jongallant.azure.identity.extensions;

import com.azure.identity.DefaultAzureCredential;
import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DefaultAzureServiceBusCredential}.
 */
public class DefaultAzureServiceBusCredentialTest {

    /**
     * Unit test to use {@link DefaultAzureCredential} to create a {@link TopicClient} and send a message.
     * @throws ServiceBusException
     * @throws InterruptedException
     */
    @Test
    public void testDefaultCredential() throws ServiceBusException, InterruptedException {
        TopicClient topicClient = new TopicClient("jongsb2", "srnagartopic",
            new ClientSettings(new DefaultAzureServiceBusCredential()));
        topicClient.send(new Message("hello")); // if this fails, exception will be thrown failing the test
        topicClient.close();
    }
}
