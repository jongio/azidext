using DotNetEnv;
using Microsoft.Azure.ServiceBus;
using System;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Xunit;

namespace JonGallant.Azure.Identity.Extensions.Tests.ServiceBus
{
    public class ServiceBusTests
    {
        [Fact]
        public async void TopicClientTests()
        {
            Env.Load("../../../.env");

            var client = new TopicClient(Environment.GetEnvironmentVariable("AZURE_SERVICE_BUS_ENDPOINT"),
                                         Environment.GetEnvironmentVariable("AZURE_SERVICE_BUS_ENTITY_PATH"),
                                         new DefaultAzureServiceBusCredential());

            var messageText = "Hello World " + Guid.NewGuid().ToString("n").Substring(0, 8);

            await client.SendAsync(new Message(Encoding.UTF8.GetBytes(messageText)));

            await client.CloseAsync();

            var subscription = new SubscriptionClient(Environment.GetEnvironmentVariable("AZURE_SERVICE_BUS_ENDPOINT"),
                                         Environment.GetEnvironmentVariable("AZURE_SERVICE_BUS_ENTITY_PATH"),
                                         Environment.GetEnvironmentVariable("AZURE_SERVICE_BUS_SUBSCRIPTION_NAME"),
                                         new DefaultAzureServiceBusCredential());

            var messageHandlerOptions = new MessageHandlerOptions((ExceptionReceivedEventArgs exceptionReceivedEventArgs) =>
            {
                Console.WriteLine($"Message handler encountered an exception {exceptionReceivedEventArgs.Exception}.");
                var context = exceptionReceivedEventArgs.ExceptionReceivedContext;
                Console.WriteLine("Exception context for troubleshooting:");
                Console.WriteLine($"- Endpoint: {context.Endpoint}");
                Console.WriteLine($"- Entity Path: {context.EntityPath}");
                Console.WriteLine($"- Executing Action: {context.Action}");
                return Task.CompletedTask;
            })
            {
                // Maximum number of concurrent calls to the callback ProcessMessagesAsync(), set to 1 for simplicity.
                // Set it according to how many messages the application wants to process in parallel.
                MaxConcurrentCalls = 1,

                // Indicates whether MessagePump should automatically complete the messages after returning from User Callback.
                // False below indicates the Complete will be handled by the User Callback as in `ProcessMessagesAsync` below.
                AutoComplete = false
            };

            subscription.RegisterMessageHandler(async (Message message, CancellationToken cancellationToken) =>
            {
                var body = Encoding.UTF8.GetString(message.Body);
                Console.WriteLine($"Received message: SequenceNumber:{message.SystemProperties.SequenceNumber} Body:{body}");

                Assert.Equal(body, messageText);
                // Complete the message so that it is not received again.
                // This can be done only if the subscriptionClient is created in ReceiveMode.PeekLock mode (which is the default).

                await subscription.CompleteAsync(message.SystemProperties.LockToken);
            }, messageHandlerOptions);
        }
    }
}
