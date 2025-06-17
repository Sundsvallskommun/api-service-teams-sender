package se.sundsvall.teamssender.api.configuration;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.bind.validation.ValidationBindHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import se.sundsvall.teamssender.api.service.MicrosoftGraphTeamsSender;

import static java.util.Objects.nonNull;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerBeanDefinition;

import java.util.Properties;


@Component
class TeamsSenderBeanFactory implements BeanFactoryPostProcessor, ApplicationContextAware, InitializingBean {

    static final String SMTP_TEAMS_SENDER_BEAN_NAME = "smtp-teams-sender-";
    static final String MICROSOFT_GRAPH_TEAMS_SENDER_BEAN_NAME = "ms-graph-teams-sender-";
    static final String DEFAULT_PROPERTIES = "integration.email.default-properties";
    static final String INSTANCES = "integration.email.instances";

    private Environment environment;
    private Validator validator;
    private Properties defaultProperties;
    private Map<String, TeamsSenderProperties> teamsSenderPropertiesByMunicipalityId;


    @Override
    public void afterPropertiesSet() throws Exception {
        final var validationBindHandler = new ValidationBindHandler(new SpringValidatorAdapter(validator));
        final var binder = Binder.get(environment);

        // Bind/load (or create empty) default properties
        defaultProperties = binder.bindOrCreate(DEFAULT_PROPERTIES, Bindable.of(Properties.class), validationBindHandler);
        // Bind/load instance properties
        teamsSenderPropertiesByMunicipalityId = binder.bind(
                INSTANCES, Bindable.mapOf(String.class, TeamsSenderProperties.class), validationBindHandler).get();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final var beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;

        teamsSenderPropertiesByMunicipalityId.forEach((municipalityId, teamsSenderProperties) -> {
            final var basicSet = nonNull(teamsSenderProperties.basic);
            final var azureSet = nonNull(teamsSenderProperties.azure);

            // Make sure that exactly one of "basic" and "azure" is set, and validate the one that actually is
            if ((!basicSet && !azureSet) || (basicSet && azureSet)) {
                throw new BeanCreationException("Exactly one of SMTP 'basic' or 'azure' properties must be set");
            } else if (basicSet) {
                validator.validate(teamsSenderProperties.basic);

                // Merge the default properties with the SMTP server properties, with
                // values from the latter possibly overriding defaults
                final var mergedProperties = new Properties();
                mergedProperties.putAll(defaultProperties);
                if (nonNull(teamsSenderProperties.basic.properties)) {
                    mergedProperties.putAll(teamsSenderProperties.basic.properties);
                }

                registerSmtpTeamsSender(beanDefinitionRegistry, municipalityId, teamsSenderProperties, mergedProperties);
            } else {
                validator.validate(teamsSenderProperties.azure);

                registerMicrosoftGraphTeamsSender(beanDefinitionRegistry, municipalityId, teamsSenderProperties);
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    static final String MICROSOFT_GRAPH_TEAMS_SENDER_BEAN_NAME = "ms-graph-teams-sender-";

    void registerMicrosoftGraphTeamsSender(final BeanDefinitionRegistry beanDefinitionRegistry, final String municipalityId, final TeamsSenderProperties teamsSenderProperties) {
        final var beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(MicrosoftGraphTeamsSender.class)
                .addConstructorArgValue(createGraphServiceClient(teamsSenderProperties.azure))
                .addPropertyValue("municipalityId", municipalityId)
                .getBeanDefinition();

        registerBeanDefinition(beanDefinitionRegistry, MICROSOFT_GRAPH_TEAMS_SENDER_BEAN_NAME + municipalityId, beanDefinition);
    }

    GraphServiceClient createGraphServiceClient(final TeamsSenderProperties.Azure azureTeamsSenderProperties) {
        final var clientSecretCredential = new ClientSecretCredentialBuilder()
                .tenantId(azureTeamsSenderProperties.tenantId)
                .clientId(azureTeamsSenderProperties.clientId)
                .clientSecret(azureTeamsSenderProperties.clientSecret)
                .build();
        return new GraphServiceClient(clientSecretCredential, azureTeamsSenderProperties.scope);
    }



    @Validated
        record TeamsSenderProperties(

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
