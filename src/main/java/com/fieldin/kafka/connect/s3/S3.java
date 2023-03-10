package com.fieldin.kafka.connect.s3;

import static com.fieldin.kafka.connect.s3.S3ConfigurationConfig.HEADERS_USE_EXPECT_CONTINUE_CONFIG;
import static com.fieldin.kafka.connect.s3.S3ConfigurationConfig.REGION_CONFIG;
import static com.fieldin.kafka.connect.s3.S3ConfigurationConfig.S3_ENDPOINT_URL_CONFIG;
import static com.fieldin.kafka.connect.s3.S3ConfigurationConfig.WAN_MODE_CONFIG;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.PredefinedClientConfigurations;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.util.Map;


public class S3 {

  public static AmazonS3 s3client(Map<String, String> props) {
    S3ConfigurationConfig config = new S3ConfigurationConfig(props);
    return newS3Client(config);
  }

  /**
   * Creates S3 client's configuration. This method currently configures the AWS client retry policy
   * to use full jitter. Visible for testing.
   *
   * @param config the S3 configuration.
   * @return S3 client's configuration
   */
  private static ClientConfiguration newClientConfiguration(S3ConfigurationConfig config) {

    ClientConfiguration clientConfiguration = PredefinedClientConfigurations.defaultConfig();
    clientConfiguration.withUserAgentPrefix("Spredfast Kafka-S3 Connect / 1.0");
    clientConfiguration.withUseExpectContinue(
        Boolean.valueOf(config.getString(HEADERS_USE_EXPECT_CONTINUE_CONFIG)));
    return clientConfiguration;
  }


  /**
   * Creates and configures S3 client. Visible for testing.
   *
   * @param config the S3 configuration.
   * @return S3 client
   */
  private static AmazonS3 newS3Client(S3ConfigurationConfig config) {
    ClientConfiguration clientConfiguration = newClientConfiguration(config);
    AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
        .withAccelerateModeEnabled(
            Boolean.valueOf(config.getString(WAN_MODE_CONFIG))
        )
        .withPathStyleAccessEnabled(true)
        .withClientConfiguration(clientConfiguration);

    String region = config.getString(REGION_CONFIG);
    String url = config.getString(S3_ENDPOINT_URL_CONFIG);
    if (url == null || url.equals("")) {
      builder = "us-east-1".equals(region)
          ? builder.withRegion(Regions.US_EAST_1)
          : builder.withRegion(region);
    } else {
      builder = builder.withEndpointConfiguration(
          new AwsClientBuilder.EndpointConfiguration(url, region)
      );
    }

    return builder.build();
  }

}
