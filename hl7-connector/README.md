# HL7 Thingsboard Gateway Connector

The `HL7 Thingsboard Gateway Connector` is a Spring Boot 3 and Apache Camel 4 project, in order to integrate HL7v2
messages into Thingsboard.

## Features

* Provides an HL7 endpoint that will process ORU-R01 messages.
* Sends telemetry updates from HL7 messages to the redis telemetry stream.
* Sends client attribute updates to the redis client attributes stream.

### HL7 Connector

A connector is now a component that connects to external systems or devices and transforms messages to be routed to the
proper redis streams: telemetry and attributes.

[hl7-connector](./hl7-connector) imports the `thingsboard-smartgw-connector-starter` dependency so we will have a
TbSmartGatewayPublisher instance that will publish to the corresponding redis streams: telemetry and attributes.

```java[hl7-connector/src/main/java/es/omarall/thingsboard/smartgw/connector/hl7/HL7Connector.java]

```yaml[hl7-connector/pom.yml]
<dependencies>
        <!-- ...  -->    
        <dependency>
            <groupId>es.omarall</groupId>
            <artifactId>thingsboard-smartgw-connector-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <!-- ...  -->
</dependencies>
```

## Getting Started

### Prerequisites

* Docker installed on your machine
* Java 17+
* Maven 3+ installed
* A running Thingsboard server or a [Thingsboard cloud] account.

### Installation

1. Clone this repository: `git clone https://github.com/oalles/thingsboard-smartgw`
2. Navigate to the project directory: `hl7-connector`
3. Build the project using Maven: `mvn clean install -DskipTests`
4. Configure the HL7 Connector in `application.properties`.

```yaml[hl7-connector/src/main/resources/application.yml]
# This is the endpoint of incoming requests - HL7 Server.
hl7-server:
  hostname: localhost
  port: 8888

# Utility Route - HL7v2 message producer from file definitions
file-producer:
  enabled: true # Enables a utility route
  input-dir: /tmp/hl7 # Drop HLv2 messages into this folder for testing and development
  delay-in-seconds: 5
```

5. Run the Spring Boot application: `mvn spring-boot:run`.

## Usage

1. Ensure your HL7 messages are correctly formatted and received by the connector.
2. The connector will automatically process each HL7 message and extract relevant data.
3. Telemetry updates will be sent to Thingsboard devices based on the HL7 content.
4. Device attributes will be updated accordingly as per the HL7 message content.
5. Monitor logs and Thingsboard dashboards to verify data transmission.

## Scenario

Let's consider a scenario where we have devices monitoring a **patient's vital signs**.
These devices will send **ORU-R01** *(Unsolicited observation result)* messages with information like this:

```text
MSH|^~\&|MonitorDevice|MonitorApp|Hospital|HIS|20230801||ORU^R01|MSG00001|P|2.3|
PID|1|123456|78901234||Doe^John^^Mr.||19900101|M|||123 Main St.^^City^ST^12345||||S|MRN123456789|1234567890|
OBR|1|123456|78901234|Vital Signs^Monitor|||20230801103000|||123^Smith^John^Dr.^^^|||||||||||ADM1234567890|20230801103000|
OBX|1|NM|9279-1^Respiratory rate^LN||20|/min||A|||F|||20230801103000|
OBX|2|NM|8867-4^Heart rate^LN||80|bpm|||||F|||20230801103000|
OBX|3|NM|8310-5^Body temperature^LN||37.0|Cel|||||F|||20230801103000|
OBX|4|NM|9279-1^Respiratory rate^LN||22|/min|||||F|||20230801103500|
OBX|5|NM|8867-4^Heart rate^LN||85|bpm|||||F|||20230801103500|
OBX|6|NM|8310-5^Body temperature^LN||37.1|Cel|||||F|||20230801103500|
```

Let's assume that the connector will be deployed on a Raspberry Pi, which will then send information to a Hospital
Management System (HMS) and forward patient information to Thingsboard cloud as well.

We'll now showcase the necessary code snippets to define a data pipeline for the HL7 data format, displaying its results
in Thingsboard:

````java
@Bean
public RouteBuilder routeBuilder(){
        return new RouteBuilder(){
@Override
public void configure(){
        // Route to receive HL7 messages from the HL7 TCP port
        from("netty:tcp://{{hl7-server.hostname}}:{{hl7-server.port}}?sync=true&decoders=#hl7decoder&encoders=#hl7encoder")
        .unmarshal().hl7(true)
        .log(LoggingLevel.DEBUG,"\n\nHeaders: ${headers}\nHL7 Message: ${body}")
        .choice()
        .when(header(HL7Constants.HL7_TRIGGER_EVENT).isEqualTo(Constants.RO1_TRIGGER_EVENT))
        .bean("tbOruR01Processor","process") # Route to proper message processor based on event type
        .otherwise()
        .process(exchange->exchange.getIn().setBody(new ACK())) // TODO: Default Message processor
        .end()
        .marshal().hl7()
        .log(LoggingLevel.DEBUG,"HL7 Response: ${body}")
        .routeId("hl7-inbound-route");
        }
        };
        }
````

```java

@Service("tbOruR01Processor")
@RequiredArgsConstructor
@Slf4j
public class TBORUR01Processor implements DeviceNameExtractor<Map<String, Object>>,
        AttributesExtractor<Map<String, Object>>,
        TelemetryValuesExtractor<ORU_R01> {

    private final TbSmartGatewayPublisher tbSmartGatewayPublisher;
    private final ObjectMapper objectMapper;
    private final ORUR01Utils oruR01Utils;

    public ACK process(@Body ORU_R01 oruMessage, @Headers Map<String, Object> headers) {

        // Optionally: Send to HMS (Hospital Management System) here or in a separate route or processor
        // ...

        // And send to TB

        // 1. Extract Device Name
        String deviceName = this.extractDeviceName(headers);

        // 2. Extract attributes
        ObjectNode attributes = this.extractAttributeValues(headers);

        // 3. Build Telemetry
        List<TsValues> telemetryValues = this.extractTelemetryValues(oruMessage);

        // Send to REDIS ~ TO TB
        tbSmartGatewayPublisher.sendTelemetry(deviceName, telemetryValues);
        tbSmartGatewayPublisher.sendAttributes(deviceName, attributes);

        return this.buildAckFromHeaders(headers);
    }

    // Base connector interface implementations
    // ...
}
```

In this code snippet, we define a route using Apache Camel to receive HL7 messages from the HL7 TCP port.
The messages are unmarshaled from the HL7 format, processed based on event types, and then forwarded to Thingsboard for
display and further processing.

The code demonstrates how Apache Camel can be used to handle HL7 messages effectively in a real-world scenario.




