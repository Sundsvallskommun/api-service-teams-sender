package se.sundsvall.teamssender.configuration;

import static java.util.Objects.nonNull;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
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
import se.sundsvall.teamssender.service.MicrosoftGraphTeamsSender;

@Component
class TeamsSenderBeanFactory implements BeanFactoryPostProcessor, ApplicationContextAware, InitializingBean {

	static final String MICROSOFT_GRAPH_TEAMS_SENDER_BEAN_NAME = "ms-graph-teams-sender-";
	static final String INSTANCES = "integration.teams.instances";

	private Environment environment;
	private Validator validator;
	private Map<String, TeamsSenderProperties> teamsSenderPropertiesByMunicipalityId;

	@Override
	public void afterPropertiesSet() {
		final var validationBindHandler = new ValidationBindHandler(new SpringValidatorAdapter(validator));
		final var binder = Binder.get(environment);

		// Bind/load instance properties
		teamsSenderPropertiesByMunicipalityId = binder.bind(
			INSTANCES, Bindable.mapOf(String.class, TeamsSenderProperties.class), validationBindHandler).get();
	}

	@Override
	public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
		final var beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;

		teamsSenderPropertiesByMunicipalityId.forEach((municipalityId, teamsSenderProperties) -> {
			final var azureSet = nonNull(teamsSenderProperties.azure);

			// Make sure that "azure" is set, and validate it is
			if ((!azureSet)) {
				throw new BeanCreationException("Azure must be set");

			} else {
				validator.validate(teamsSenderProperties.azure);

				registerMicrosoftGraphTeamsSender(beanDefinitionRegistry, municipalityId, teamsSenderProperties);
			}
		});
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		environment = applicationContext.getEnvironment();
		validator = applicationContext.getBean(Validator.class);
	}

	void registerBeanDefinition(final BeanDefinitionRegistry beanDefinitionRegistry, final String beanName, final BeanDefinition beanDefinition) {
		beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
	}

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

		Azure azure) {

		private static final String NOT_BLANK_MESSAGE = "must not be blank";

		record Azure(
			@NotBlank(message = NOT_BLANK_MESSAGE) String tenantId,
			@NotBlank(message = NOT_BLANK_MESSAGE) String clientId,
			@NotBlank(message = NOT_BLANK_MESSAGE) String clientSecret,
			@DefaultValue("https://graph.microsoft.com/.default") String scope) {
		}
	}

}
