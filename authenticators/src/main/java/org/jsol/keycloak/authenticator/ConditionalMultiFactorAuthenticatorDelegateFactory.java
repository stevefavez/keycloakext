package org.jsol.keycloak.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.services.DefaultKeycloakSessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.keycloak.provider.ProviderConfigProperty.*;

/**
 * /**
 * An {@link org.keycloak.authentication.AuthenticatorFactory} for {@link ConditionalMultiFactorAuthenticatorDelegate}.
 *
 * @author <a href="mailto:favez.steve@gmail.com">Steve Favez</a>
 */

public class ConditionalMultiFactorAuthenticatorDelegateFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "conditional-2AF-delegate-form";
    public static final String SECOND_FACTOR_AUTHENTICATOR_LIST_ATTRIBUTE = "secondFactorAuthenticatorList";
    public static final String SECONDAF_CONTROL_USER_ATTRIBUTE = "secondAFControlAttribute";
    public static final String SKIP = "skip";
    public static final String FORCE = "force";
    public static final String SKIP_SECONDAF_ROLE = "skipSecondAFRole";
    public static final String FORCE_SECONDAF_ROLE = "forceSecondAFRole";
    public static final String SKIP_SECONDAF_FOR_HTTP_HEADER = "noSecondAFRequiredForHeaderPattern";
    public static final String FORCE_SECONDAF_FOR_HTTP_HEADER = "forceSecondAFForHeaderPattern";
    public static final String DEFAULT_SECONDAF_OUTCOME = "defaultSecondAFOutcome";

    private KeycloakSessionFactory keycloakSessionFactory ;

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.OPTIONAL,
            AuthenticationExecutionModel.Requirement.DISABLED};

    @Override
    public Authenticator create(KeycloakSession session) {
        return new ConditionalMultiFactorAuthenticatorDelegate(session);
    }

    @Override
    public void init(Config.Scope config) {
        System.out.println(" *******************  in init with config  **********************, " + config);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        this.keycloakSessionFactory = factory ;
    }

    @Override
    public void close() {
        //NOOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return UserCredentialModel.TOTP;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }


    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "Conditional Second Factor Form";
    }

    @Override
    public String getHelpText() {
        return "Delegate a Second Factor of authentication to an existing authenticator Form. Only shown if required based on the configured conditions.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty availableSecondFactorAuthenticator = new ProviderConfigProperty() ;
        availableSecondFactorAuthenticator.setType( ProviderConfigProperty.MULTIVALUED_STRING_TYPE );
        availableSecondFactorAuthenticator.setName(SECOND_FACTOR_AUTHENTICATOR_LIST_ATTRIBUTE);
        availableSecondFactorAuthenticator.setLabel("Available second factor authenticators");
        availableSecondFactorAuthenticator.setHelpText("List of Second Factor Authenticator available for the end user");
        availableSecondFactorAuthenticator.setSecret(false);
        availableSecondFactorAuthenticator.setDefaultValue(null);
        availableSecondFactorAuthenticator.setOptions(getAuthenticatorFactoryIds());


        ProviderConfigProperty forceOtpUserAttribute = new ProviderConfigProperty();
        forceOtpUserAttribute.setType(STRING_TYPE);
        forceOtpUserAttribute.setName(SECONDAF_CONTROL_USER_ATTRIBUTE);
        forceOtpUserAttribute.setLabel("Second Authentication Factor control User Attribute");
        forceOtpUserAttribute.setHelpText("The name of the user attribute to explicitly control Second Factor auth. " +
                "If attribute value is 'force' then second factor is always required. " +
                "If value is 'skip' the second factor auth is skipped. Otherwise this check is ignored.");

        ProviderConfigProperty skipOtpRole = new ProviderConfigProperty();
        skipOtpRole.setType(ROLE_TYPE);
        skipOtpRole.setName(SKIP_SECONDAF_ROLE);
        skipOtpRole.setLabel("Skip Second Factor Authenticator for Role");
        skipOtpRole.setHelpText("Second Factor Authenticator is always skipped if user has the given Role.");

        ProviderConfigProperty forceOtpRole = new ProviderConfigProperty();
        forceOtpRole.setType(ROLE_TYPE);
        forceOtpRole.setName(FORCE_SECONDAF_ROLE);
        forceOtpRole.setLabel("Force Second Factor Authenticator for Role");
        forceOtpRole.setHelpText("Second Factor Authenticator is always required if user has the given Role.");

        ProviderConfigProperty skipOtpForHttpHeader = new ProviderConfigProperty();
        skipOtpForHttpHeader.setType(STRING_TYPE);
        skipOtpForHttpHeader.setName(SKIP_SECONDAF_FOR_HTTP_HEADER);
        skipOtpForHttpHeader.setLabel("Skip Second Factor Authenticator for Header");
        skipOtpForHttpHeader.setHelpText("Second Factor Authenticator is skipped if a HTTP request header does matches the given pattern." +
                "Can be used to specify trusted networks via: X-Forwarded-Host: (1.2.3.4|1.2.3.5)." +
                "In this case requests from 1.2.3.4 and 1.2.3.5 come from a trusted source.");
        skipOtpForHttpHeader.setDefaultValue("");

        ProviderConfigProperty forceOtpForHttpHeader = new ProviderConfigProperty();
        forceOtpForHttpHeader.setType(STRING_TYPE);
        forceOtpForHttpHeader.setName(FORCE_SECONDAF_FOR_HTTP_HEADER);
        forceOtpForHttpHeader.setLabel("Force Second Factor Authenticator for Header");
        forceOtpForHttpHeader.setHelpText("Second Factor Authenticator required if a HTTP request header matches the given pattern.");
        forceOtpForHttpHeader.setDefaultValue("");

        ProviderConfigProperty defaultOutcome = new ProviderConfigProperty();
        defaultOutcome.setType(LIST_TYPE);
        defaultOutcome.setName(DEFAULT_SECONDAF_OUTCOME);
        defaultOutcome.setLabel("Fallback Second Factor Authenticator handling");
        defaultOutcome.setOptions(asList(SKIP, FORCE));
        defaultOutcome.setHelpText("What to do in case of every check abstains. Defaults to force Second Factor Authenticator authentication.");

        return asList(availableSecondFactorAuthenticator, forceOtpUserAttribute, skipOtpRole, forceOtpRole, skipOtpForHttpHeader, forceOtpForHttpHeader, defaultOutcome);
    }

    private List<String> getAuthenticatorFactoryIds() {
        DefaultKeycloakSessionFactory sessionFactory = new DefaultKeycloakSessionFactory() ;
        sessionFactory.init();
        final List<ProviderFactory> providerFactories = sessionFactory.getProviderFactories(Authenticator.class);
        return providerFactories.stream().map((ProviderFactory factory) -> {
            return factory.getId();
        }).collect(Collectors.toList());
    }

}
