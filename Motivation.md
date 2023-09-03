# Motivation for a Custom Thingsboard Gateway Implementation based on Redis

[Thingsboard project](https://github.com/thingsboard/) provides its
own [Thingsboard IoT Gateway](https://github.com/thingsboard/thingsboard-gateway) as a python solution, designed to run
on a Linux based microcomputers that support Python, in order to integrate devices or external systems to Thingsboard.

There
are [some limitations](https://thingsboard.io/docs/paas/user-guide/integrations/#platform-integrations-vs-iot-gateway)
to be aware though:

* IoT Gateway is designed for local network deployments.
* IoT Gateway is designed to support < 1000 devices.

> There is an opportunity for a Thingsboard gateway solution that provides scalability, high throughput, allows
> clusterized, cloud or edge deployments, and complex integration models.

## Thingsboard Gateway Architecture

As stated in the Thingsboard IOT Gateway [description](https://thingsboard.io/docs/iot-gateway/what-is-iot-gateway/),
the main componentes are: *connectors*, *converters*, *event storage* and the *thingsboard client*.

The absence of an *integration layer* and even a *data processing layer* makes this solution seem solely designed as a
data ingest solution.

*Connectors* and *converters* are indeed components of a *data integration layer*. They are essential parts of the
integration process that ensure communication and data transformation between devices, 3rd party systems, applications,
and data sources. But an *integration layer* encompasses a range of components and features beyond just *connectors* and
*converters*. By incorporating these additional aspects into our integration layer, we could create a versatile
framework that not only connects systems but also provides advanced capabilities for handling diverse integration
complexities: from real-time data streaming; data filtering and aggregation; near-zero latency read operations; instant
analytics; high availability.

**Requirement**: Open to include:

1. an **integration layer** that not only includes connectors and converters but embrace the principles
   of [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/).
2. a **data processing layer** that can be further broken down into discrete stages, each of which can run on different
   components to ensure efficient and effective data processing.

## Gateway Architecture based on Redis

![TB Smart Gateway diagram](./images/tb-smart-gateway.png)

### Redis as the Event Storage (but not just)

Redis brings a versatile *multi-model database* that is an ideal fit for the challenging demands of the Internet of
Things (IoT). Redis is incredibly fast, handling millions of writes per second with less than a millisecond of delay.
Its compact size (<5MB) makes it even perfect for resource-limited setups, from ARM32 to x64-based hardware.

[Redis Streams](https://redis.io/docs/data-types/streams/) are a native data structure in Redis specifically designed
for handling real-time data streams, making them a strong candidate for storing events. Features:

* *Order and Time*: Redis Streams maintain the sequence of events in the order they were added.

* *Persistence and Durability*: Redis Streams can be configured to maintain events both in memory and on disk, providing
  persistence options that ensure data durability even in restart or failure situations.

* *Real-time Consumption and Consumer Groups*: Redis Streams support real-time event reading through a subscription
  model (consumers). Consumer groups in Redis Streams allow events to be distributed among multiple consumers, which is
  beneficial for scaling event processing.

* *Controlled Memory Size*: While Redis Streams keep events in memory, their compact structure and the ability to
  configure expiration policies can help control memory usage, retaining only relevant events for a certain period.

* *Query and Read Capabilities*: Redis Streams offer query operations that allow us to retrieve events based on specific
  criteria, facilitating search and data retrieval.

Moreover, by incorporating Redis Streams as our event storage solution, we harness the full power of Redis. This opens
up the opportunity to further enhance our system by integrating a data streaming platform based on Redis Streams.
Leveraging Redis Gears, a Redis serverless data processing engine, allows us to create data processing pipelines that
transform and manipulate streaming data in real time. This approach not only enhances the extensibility of our solution
to accommodate complex data flows if needed but also invites the inclusion of additional Redis modules such as Redis AI.
Redis AI, an AI inference engine, enables us to perform inferencing on machine learning models stored in formats like
TensorFlow, PyTorch, ONNX, and more, seamlessly integrated into our data stream.

### redisx-mqtt-bridge as a Thingsboard Gateway Adapter

[redisx-mqtt-bridge](./redisx-mqtt-bridge) is a service which connects redis streams and MQTT topics, allowing the
seamless transmission of bytes through reactive APIs. Both directions are supported, from Redis to MQTT and vice versa.

Given the [Thingsboard Mqtt Gateway api](https://thingsboard.io/docs/reference/gateway-mqtt-api/), and this
*redisx-mqtt-bridge* configuration:

```yaml 
mqtt:
  host: thingsboard.cloud # broker.hivemq.com
  username: O5iuAs0O1MVFH23lbmL0 # The TB Gateway device access token
#  password:

application:
  bridge-definitions:
    - name: telemetry
      direction: REDIS_TO_MQTT
      streamKey: telemetry # Send telemetry here
      topic: v1/gateway/telemetry
    - name: attributes # 
      direction: REDIS_TO_MQTT
      streamKey: attributes # client attribute updates
      topic: v1/gateway/attributes
    - name: attributes updates
      direction: MQTT_TO_REDIS
      streamKey: attribute.updates # shared attribute updates on attribute.updates stream
      topic: v1/gateway/attributes
```

This way we are creating a **middleware or adapter** that effectively transforms
the [Thingsboard Mqtt Gateway api](https://thingsboard.io/docs/reference/gateway-mqtt-api/) into a *ThingsBoard Redis
Gateway API*. The result is an extended functionality that allows data to flow seamlessly from Redis-based streams into
the ThingsBoard platform.

## Custom Connectors

Connectors will be the services that connect to external systems or devices, providing processing pipelines, to perform
transformative operations on incoming data, to build meaningful telemetry and client attribute payloads, to be routed to
the
proper **redis streams**: telemetry and attributes.

#### Redis Gears, as serverless data processing engine

Once we have Redis and Redis Streams in place, we can leverage [RedisGears](https://oss.redis.com/redisgears/), as the
engine for data processing in Redis supporting event-driven processing of Redis data.
It allows us to define customized functions, known as Gears functions, that specify the desired data transformations.
These functions are executed directly within the Redis environment, eliminating the need for data movement and promoting
low-latency processing.
> you write functions that describe how your data should be processed. You then submit this code to your Redis
> deployment for remote execution

##### Complex Thingsboard Connector Scenario

Given the [Redis EdgeRealtimeVideoAnalytics](https://github.com/RedisGears/EdgeRealtimeVideoAnalytics) and the reference
architecture described, it would be very easy to integrate external video cameras and video analytics into Thingsboard.
Once we have the object detections, we could build for instance telemetry payload, and send to the telemetry redis
stream, or client attributes updates, and send to the attributes redis stream.

#### Apache Camel as the Integration Framework for Java Connectors

[Apache Camel](https://camel.apache.org/manual/faq/what-is-camel.html) abstracts the complexities of integrating with
external systems or services, offering a range of features, including:

* **Connectors** for diverse systems: Apache Camel provides connectors for hundreds of systems and protocols, all
  accessible through a unified API or DSL. This abstraction shields developers from dealing with the intricate details
  of each system, promoting protocol agnosticism.

* **[Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/) (EIPs)**: With a comprehensive
  array of Enterprise Integration Pattern (EIP) implementations, Apache Camel addresses integration challenges such as
  message routing, mediation, and transformation.

* **Advanced Data Manipulation**: Apache Camel is adept at performing intricate data transformations, aggregations,
  enrichments, and real-time processing. These capabilities make it a versatile solution for complex data processing
  tasks.
* **Error Handling and Observability**: Apache Camel provides robust error handling and observability capabilities,
  empowering developers and operations teams to gain insights into the behavior, performance, and health of integration
  and data processing workflows.

As a result of these features, Apache Camel is a natural fit for both an integration layer and a data processing layer.

In the project, we provide a sample [HL7-Connector](./hl7-connector) that integrates HL7v2 messages into Thingsboard.

## UPDATE: Video Scenario

One common concern when considering Apache Camel for integration is the availability of specific clients or connectors.
Some might wonder if Apache Camel remains a viable choice when their
desired [component](https://camel.apache.org/components/4.0.x/index.html) isn't readily available.

We 'll explore a hypothetical situation: We need to add a video connector to our gateway to connect to a video source,
perform person and car detections on each frame, and transmit the results to Thingsboard.
All be built on top of Apache Camel.

To achieve this, we create two components:

· [Video-IO](./camel-video-io) Camel Component: This component facilitates video input and output for Apache Camel.
· [Video-Connector](./video-connector) for the Gateway: As a Camel route, leveraging AI video detections video analytics
with Thingsboard.
