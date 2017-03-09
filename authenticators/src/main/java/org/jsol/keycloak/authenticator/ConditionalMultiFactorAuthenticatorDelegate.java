package org.jsol.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

/**
 *
 * This keycloak authenticator has two main functionality.
 *      1. Allow to delegate 2FA to an existing second factor authenticator (exemple, ask for secret question, otp, sms...)
 *      2. Allow to be executed only according to some context values (for exemple, IP Range, etc...)
 *
 * @author <a href="mailto:favez.steve@gmail.com">Steve Favez</a>
 *
 * http://blog.keycloak.org/2016/07/loading-providers-and-themes-from-maven.html
 */
public class ConditionalMultiFactorAuthenticatorDelegate implements Authenticator {

    private final static Logger LOGGER = Logger.getLogger(ConditionalMultiFactorAuthenticatorDelegate.class) ;

    public static final String SECONDFACTOR_AUTHENTICATOR_SELECTOR_FTL = "secondfactor-authenticator-selector.ftl";
    // store keycloak session in order to be able to create second factor authenticator
    private KeycloakSession keycloakSession ;


    public ConditionalMultiFactorAuthenticatorDelegate(KeycloakSession session) {
        this.keycloakSession = session ;
    }

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        final HttpRequest httpRequest = authenticationFlowContext.getHttpRequest();
        LOGGER.debug("trying to authenticate with a given httpRequest");

        boolean secondfactorRequired = true ;

        //if second factor authentication is required and ready, ask to choose the proper authenticator
        if( secondfactorRequired ) {
            showSecondFactorChoice( authenticationFlowContext ) ;
        } else {
            authenticationFlowContext.success();
        }

    }

    private void showSecondFactorChoice(AuthenticationFlowContext authenticationFlowContext) {
        final List<String> availableChoices = Arrays.asList("sms", "otp", "card");
        Response challenge = authenticationFlowContext.form().setAttribute("secondafoptions", availableChoices).createForm(SECONDFACTOR_AUTHENTICATOR_SELECTOR_FTL);
        authenticationFlowContext.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        MultivaluedMap<String, String> formData = authenticationFlowContext.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            authenticationFlowContext.cancelLogin();
            return;
        }
        String foundNote  = authenticationFlowContext.getClientSession().getNote( "selectedSystem" ) ;
        if (foundNote == null) {
            authenticationFlowContext.getClientSession().setNote("selectedSystem", "auth-otp-form");
            AuthenticatorFactory factory = (AuthenticatorFactory) this.getKeycloakSession().getKeycloakSessionFactory().getProviderFactory(Authenticator.class, "auth-otp-form");
            final Authenticator secondFactorAuthenticator = factory.create(getKeycloakSession());
            secondFactorAuthenticator.authenticate(authenticationFlowContext);
        } else {
            System.out.println("note is :  " + foundNote) ;
            AuthenticatorFactory factory = (AuthenticatorFactory) this.getKeycloakSession().getKeycloakSessionFactory().getProviderFactory(Authenticator.class, foundNote);
            final Authenticator secondFactorAuthenticator = factory.create(getKeycloakSession());
            secondFactorAuthenticator.action( authenticationFlowContext );
        }


    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }

    public KeycloakSession getKeycloakSession() {
        return keycloakSession;
    }
}
