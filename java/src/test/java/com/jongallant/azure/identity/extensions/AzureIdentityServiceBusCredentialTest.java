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
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link AzureIdentityServiceBusCredential}.
 */
public class AzureIdentityServiceBusCredentialTest {

  private static final String AZURE_BASE_NAME = "AZURE_BASE_NAME";
  private static Dotenv ENVIRONMENT;

  /**
   * Loads environment variables from a properties file named ".env" that should be available in the classpath. The "
   * .env" file should have definition for AZURE_BASE_NAME which has the prefix for Service Bus namespace.
   *
   * Template for this file can be found at the root of this repository name .env.temp
   */
  @BeforeAll
  static void loadEnvironmentProperties() {
    ENVIRONMENT = Dotenv.load();
  }

  /**
   * Unit test to use {@link DefaultAzureCredential} to create a {@link TopicClient} and send a message.
   *
   * @throws ServiceBusException
   * @throws InterruptedException
   */
  @Test
  public void testDefaultCredential() throws Exception {
    ClientSettings clientSettings = new ClientSettings(new AzureIdentityServiceBusCredential());
    String endpoint = ENVIRONMENT.get(AZURE_BASE_NAME) + "sbns";
    String topicName = "topic1";
    TopicClient topicClient = new TopicClient(endpoint, topicName, clientSettings);
    String message = "hello " + UUID.randomUUID().toString().substring(0, 8);

    // send a message
    topicClient.sendAsync(new Message(message)).get();
    topicClient.closeAsync().get();

    // receive the message
    String subscriptionPath = "sub1";
    SubscriptionClient subscriptionClient = new SubscriptionClient(endpoint, subscriptionPath, clientSettings, ReceiveMode.RECEIVEANDDELETE);

    CountDownLatch countDownLatch = new CountDownLatch(1); // expect to receive just 1 message
    subscriptionClient.registerMessageHandler(
        new IMessageHandler() {
          @Override
          public CompletableFuture<Void> onMessageAsync(IMessage iMessage) {
            Assertions.assertEquals(message,
                new String(iMessage.getMessageBody().getBinaryData().get(0), StandardCharsets.UTF_8));
            countDownLatch.countDown();
            return CompletableFuture.completedFuture(null);
          }

          @Override
          public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
            Assertions.fail(throwable);
          }
        }, Executors.newCachedThreadPool());
    Assertions.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    subscriptionClient.closeAsync().get();
  }
}
