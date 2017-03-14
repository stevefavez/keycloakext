package org.jsol.keycloak.authenticator;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;

/**
 * Created by Steve Favez on 14.03.2017.
 */
public class MatrixCardAuthenticator implements Authenticator {

    public static final String MATRIX_CARD_AUTHENTICATOR = "matrixcard-authenticator.ftl";

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        Response challenge = authenticationFlowContext.form().createForm( MATRIX_CARD_AUTHENTICATOR ) ;
        authenticationFlowContext.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        authenticationFlowContext.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true ;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
