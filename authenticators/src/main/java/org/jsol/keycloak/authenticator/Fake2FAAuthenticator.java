package org.jsol.keycloak.authenticator;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * Fake2FA Authenticator used for test purpose if others 2fa authenticator are not configured
 * for current user -> allow to bypass and go to user account to complete others 2FA authenticator
 *
 * PLEASE, Never use it in production.
 *
 * Created by Steve Favez on 14.03.2017.
 */
public class Fake2FAAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        //ByPass authentication, accept as successfull anyway.
        authenticationFlowContext.success();
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {

    }

    @Override
    public boolean requiresUser() {
        return true ;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        //anyway, configured for and accept it
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
