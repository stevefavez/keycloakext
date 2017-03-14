package org.jsol.keycloak.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.services.DefaultKeycloakSessionFactory;

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
    public static final String SKIP_SECONDAF_IPRANGES = "skipSecondAFIpRanges";
    public static final String FORCE_SECONDAF_IPRANGES = "forceSecondIpRanges";


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
        availableSecondFactorAuthenticator.setType( ProviderConfigProperty.STRING_TYPE );
        availableSecondFactorAuthenticator.setName(SECOND_FACTOR_AUTHENTICATOR_LIST_ATTRIBUTE);
        availableSecondFactorAuthenticator.setLabel("Available second factor authenticators");
        availableSecondFactorAuthenticator.setHelpText("List of Second Factor Authenticator available for the end user - comma delimited authenticator IDs.");

        ProviderConfigProperty skip2FAIpRanges = new ProviderConfigProperty();
        skip2FAIpRanges.setType(ProviderConfigProperty.STRING_TYPE);
        skip2FAIpRanges.setName(SKIP_SECONDAF_IPRANGES);
        skip2FAIpRanges.setLabel("Skip Second Factor Authenticator for IP in Ranges");
        skip2FAIpRanges.setHelpText("Second Factor Authenticator is always skipped if client IP is in this ranges.");

        ProviderConfigProperty force2FAIpRanges = new ProviderConfigProperty();
        force2FAIpRanges.setType(ProviderConfigProperty.STRING_TYPE);
        force2FAIpRanges.setName(FORCE_SECONDAF_IPRANGES);
        force2FAIpRanges.setLabel("Force Second Factor Authenticator for IP in Ranges");
        force2FAIpRanges.setHelpText("Second Factor Authenticator is always required if client IP is in this ranges.");


        return asList(availableSecondFactorAuthenticator, skip2FAIpRanges, force2FAIpRanges);
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
