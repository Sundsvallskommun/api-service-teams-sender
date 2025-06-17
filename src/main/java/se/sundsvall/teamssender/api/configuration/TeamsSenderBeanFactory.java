package se.sundsvall.teamssender.api.configuration;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;
import se.sundsvall.teamssender.api.service.MicrosoftGraphTeamsSender;

import java.util.Properties;

import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerBeanDefinition;


public class TeamsSenderBeanFactory {

    static final String MICROSOFT_GRAPH_TEAMS_SENDER_BEAN_NAME = "ms-graph-teams-sender-";

    void registerMicrosoftGraphMailSender(final BeanDefinitionRegistry beanDefinitionRegistry, final String municipalityId, final MailSenderProperties mailSenderProperties) {
        final var beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(MicrosoftGraphTeamsSender.class)
                .addConstructorArgValue(createGraphServiceClient(mailSenderProperties.azure))
                .addPropertyValue("municipalityId", municipalityId)
                .getBeanDefinition();

        registerBeanDefinition(beanDefinitionRegistry, MICROSOFT_GRAPH_TEAMS_SENDER_BEAN_NAME + municipalityId, beanDefinition);
    }

    GraphServiceClient createGraphServiceClient(final MailSenderProperties.Azure azureMailSenderProperties) {
        final var clientSecretCredential = new ClientSecretCredentialBuilder()
                .tenantId(azureMailSenderProperties.tenantId)
                .clientId(azureMailSenderProperties.clientId)
                .clientSecret(azureMailSenderProperties.clientSecret)
                .build();
        return new GraphServiceClient(clientSecretCredential, azureMailSenderProperties.scope);
    }


        @Validated
        record MailSenderProperties(

                Basic basic,
                Azure azure) {

            private static final String NOT_BLANK_MESSAGE = "must not be blank";

            record Basic(
                    @NotBlank(message = NOT_BLANK_MESSAGE) String host,
                    @DefaultValue("25") Integer port,
                    String username,
                    String password,
                    Properties properties) {
            }

            record Azure(
                    @NotBlank(message = NOT_BLANK_MESSAGE) String tenantId,
                    @NotBlank(message = NOT_BLANK_MESSAGE) String clientId,
                    @NotBlank(message = NOT_BLANK_MESSAGE) String clientSecret,
                    @DefaultValue("https://graph.microsoft.com/.default") String scope) {
            }
        }


}
