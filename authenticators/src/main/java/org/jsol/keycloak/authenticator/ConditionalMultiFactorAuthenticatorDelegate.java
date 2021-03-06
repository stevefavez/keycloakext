package org.jsol.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This keycloak authenticator has two main functionality.
 * 1. Allow to delegate 2FA to an existing second factor authenticator (exemple, ask for secret question, otp, sms...)
 * 2. Allow to be executed only according to some context values (for exemple, IP Range, etc...)
 * <p>
 * NB : this is a skeleton class... must be completed in the future
 *
 * @author <a href="mailto:favez.steve@gmail.com">Steve Favez</a>
 *         <p>
 *         http://blog.keycloak.org/2016/07/loading-providers-and-themes-from-maven.html
 */
public class ConditionalMultiFactorAuthenticatorDelegate implements Authenticator {

    private final static Logger LOGGER = Logger.getLogger(ConditionalMultiFactorAuthenticatorDelegate.class);
    /**
     * template used to dispay second factor selector
     */
    public static final String SECONDFACTOR_AUTHENTICATOR_SELECTOR_FTL = "secondfactor-authenticator-selector.ftl";
    public static final String SECONDAF_FORM_KEY = "secondaf";
    public static final String LOGIN_FORM_KEY = "login";
    public static final String SELECTED_2FA_AUTHENTICATOR_NOTE_ATTRIBUTE = "selected.2fa.authenticator.note.attribute";
    public static final String SECONDAFOPTIONS_FORM_ATTRIBUTE = "secondafoptions";

    // store keycloak session in order to be able to create second factor authenticator
    private KeycloakSession keycloakSession;


    public ConditionalMultiFactorAuthenticatorDelegate(KeycloakSession session) {
        this.keycloakSession = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        final HttpRequest httpRequest = authenticationFlowContext.getHttpRequest();
        LOGGER.debug("trying to authenticate with a given httpRequest");

        boolean secondfactorRequired = voteIf2FARequired(authenticationFlowContext, keycloakSession);
        ;

        //if second factor authentication is required and ready, ask to choose the proper authenticator
        if (secondfactorRequired) {
            showSecondFactorChoice(authenticationFlowContext);
        } else {
            authenticationFlowContext.success();
        }
    }

    private boolean voteIf2FARequired(AuthenticationFlowContext authenticationFlowContext, KeycloakSession keycloakSession) {
        return SecondFaVoter.voteFor2FARequired( authenticationFlowContext, keycloakSession ) ;
    }

    /**
     * ask the user to select a second authenticator factor - the one he can use (the ones that are configured for him)
     *
     * @param authenticationFlowContext
     */
    private void showSecondFactorChoice(AuthenticationFlowContext authenticationFlowContext) {
        /**TODO  : the list must be based on both authenticator configuration and for each 2fa authenticator, check
         if the 2FA is configured for the current user. IE if auth-otp-form is available, it means
         that current user has a valid otp configurtion for this realm */
        final String available2FAAuthenticator = authenticationFlowContext.getAuthenticatorConfig().getConfig().get(ConditionalMultiFactorAuthenticatorDelegateFactory.SECOND_FACTOR_AUTHENTICATOR_LIST_ATTRIBUTE);
        List<String> listOfavailable2FAAuthenticator = Arrays.asList(available2FAAuthenticator.split("\\s*,\\s*"));
        if (listOfavailable2FAAuthenticator.size() == 0) {
            //for test purpose, must be removed....
            listOfavailable2FAAuthenticator = Arrays.asList("auth-otp-form", "smsAuthenticator", "matrixCardAuthenticator", "fake2faAuthenticator");
        }

        List<String> filteredUserConfigured2FAAuthenticator = listOfavailable2FAAuthenticator.stream().filter( currentAuthenticator -> isAuthenticatorConfigured( currentAuthenticator, authenticationFlowContext ) ).collect(Collectors.toList()); ;

        Response challenge = authenticationFlowContext.form().setAttribute(SECONDAFOPTIONS_FORM_ATTRIBUTE, filteredUserConfigured2FAAuthenticator).createForm(SECONDFACTOR_AUTHENTICATOR_SELECTOR_FTL);
        authenticationFlowContext.challenge(challenge);
    }

    private boolean isAuthenticatorConfigured(String currentAuthenticator, AuthenticationFlowContext authenticationFlowContext) {
        AuthenticatorFactory factory = (AuthenticatorFactory) this.getKeycloakSession().getKeycloakSessionFactory().getProviderFactory(Authenticator.class, currentAuthenticator);
        final Authenticator secondFactorAuthenticator = factory.create(getKeycloakSession());
        return secondFactorAuthenticator.configuredFor(  keycloakSession, authenticationFlowContext.getRealm(), authenticationFlowContext.getUser() ) ;
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        MultivaluedMap<String, String> formData = authenticationFlowContext.getHttpRequest().getDecodedFormParameters();

        //second af form selector.
        if (formContainsKey(SECONDAF_FORM_KEY).test(formData)) {
            if (formContainsKey(LOGIN_FORM_KEY).test(formData)) {
                //second factor selected
                showSecondFactorChallenge(authenticationFlowContext, formData.getFirst(SECONDAF_FORM_KEY));
            } else {
                //no selected, canceling login.
                authenticationFlowContext.cancelLogin();
            }
        } else {
            //case of the delegated second factor
            String foundNote = authenticationFlowContext.getClientSession().getNote(SELECTED_2FA_AUTHENTICATOR_NOTE_ATTRIBUTE);
            //case one - user clicked ok
            if (formContainsKey(LOGIN_FORM_KEY).test(formData) && (foundNote != null)) {
                AuthenticatorFactory factory = (AuthenticatorFactory) this.getKeycloakSession().getKeycloakSessionFactory().getProviderFactory(Authenticator.class, foundNote);
                final Authenticator secondFactorAuthenticator = factory.create(getKeycloakSession());
                secondFactorAuthenticator.action(authenticationFlowContext);
            } else {
                authenticationFlowContext.getClientSession().removeNote(SELECTED_2FA_AUTHENTICATOR_NOTE_ATTRIBUTE);
                showSecondFactorChoice(authenticationFlowContext);
            }
        }

    }

    private void showSecondFactorChallenge(AuthenticationFlowContext authenticationFlowContext, final String
            a2FASystem) {
        authenticationFlowContext.getClientSession().setNote(SELECTED_2FA_AUTHENTICATOR_NOTE_ATTRIBUTE, a2FASystem);
        AuthenticatorFactory factory = (AuthenticatorFactory) this.getKeycloakSession().getKeycloakSessionFactory().getProviderFactory(Authenticator.class, a2FASystem);
        final Authenticator secondFactorAuthenticator = factory.create(getKeycloakSession());
        secondFactorAuthenticator.authenticate(authenticationFlowContext);
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


    /**
     * Predicate to ensure that  a form contains a given key
     *
     * @param aKey
     * @return
     */
    static Predicate<MultivaluedMap<String, String>> formContainsKey(final String aKey) {
        return formData -> formData.containsKey(aKey) ? true : false;
    }

}
