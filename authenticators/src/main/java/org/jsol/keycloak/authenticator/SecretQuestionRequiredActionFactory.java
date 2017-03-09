package org.jsol.keycloak.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Created by Steve Favez on 19.05.2016.
 */
public class SecretQuestionRequiredActionFactory implements RequiredActionFactory {

    private static final SecretQuestionRequiredAction SINGLETON = new SecretQuestionRequiredAction();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return SINGLETON;
    }


    @Override
    public String getId() {
        return SecretQuestionRequiredAction.PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "Secret Question";
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

}