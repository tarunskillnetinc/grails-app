package uk.co.wonderlane.wlpos

import org.springframework.beans.factory.config.AbstractFactoryBean
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient

class SnsClientFactoryBean extends AbstractFactoryBean<SnsClient> {
    Region region
    AwsCredentialsProvider credentialsProvider
    String endpoint

    @Override
    Class<?> getObjectType() {
        SnsClient
    }

    @Override
    protected SnsClient createInstance() {
        def builder = SnsClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)

        if (endpoint) {
            builder.endpointOverride(URI.create(endpoint))
        }

        builder.build()
    }
}
