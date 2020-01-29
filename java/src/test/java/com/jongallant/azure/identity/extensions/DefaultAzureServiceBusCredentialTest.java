package com.jongallant.azure.identity.extensions;

import com.azure.identity.DefaultAzureCredential;
import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DefaultAzureServiceBusCredential}.
 */
public class DefaultAzureServiceBusCredentialTest {

    private static final String AZURE_SERVICE_BUS_ENDPOINT = "AZURE_SERVICE_BUS_ENDPOINT";
    private static final String AZURE_SERVICE_BUS_ENTITY_PATH = "AZURE_SERVICE_BUS_ENTITY_PATH";
    private static Properties PROPERTIES;

    @BeforeAll
    static void loadEnvironmentProperties() {
        try (InputStream inputStream = DefaultAzureServiceBusCredentialTest.class.getClassLoader().getResourceAsStream(".env.temp")) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                PROPERTIES = properties;
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Unit test to use {@link DefaultAzureCredential} to create a {@link TopicClient} and send a message.
     *
     * @throws ServiceBusException
     * @throws InterruptedException
     */
    @Test
    public void testDefaultCredential() throws Exception {
        ClientSettings clientSettings = new ClientSettings(new DefaultAzureServiceBusCredential());
        TopicClient topicClient = new TopicClient(PROPERTIES.getProperty(AZURE_SERVICE_BUS_ENDPOINT),
            PROPERTIES.getProperty(AZURE_SERVICE_BUS_ENTITY_PATH),
            clientSettings);
        String message = "hello";

        // send a message
        topicClient.sendAsync(new Message(message)).get();
        topicClient.closeAsync().get();

        // receive the message
        SubscriptionClient subscriptionClient = new SubscriptionClient(PROPERTIES.getProperty(AZURE_SERVICE_BUS_ENDPOINT),
            PROPERTIES.getProperty(AZURE_SERVICE_BUS_ENDPOINT), clientSettings, ReceiveMode.RECEIVEANDDELETE);
        subscriptionClient.registerMessageHandler(new IMessageHandler() {
            @Override
            public CompletableFuture<Void> onMessageAsync(IMessage iMessage) {
                Assertions.assertEquals(message, iMessage.getMessageBody().getValueData());
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {

            }
        }, Executors.newCachedThreadPool());
        subscriptionClient.closeAsync().get();

    }
}
