package org.jsol.keycloak.authenticator;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserCredentialModel;

import javax.ws.rs.core.Response;

/**
 * Created by Steve Favez on 19.05.2016.
 */
public class SecretQuestionRequiredAction implements RequiredActionProvider {
    public static final String PROVIDER_ID = "secret_question_config";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form().createForm("secret-question-config.ftl");
        context.challenge(challenge);

    }

    @Override
    public void processAction(RequiredActionContext context) {
        String answer = (context.getHttpRequest().getDecodedFormParameters().getFirst("secret_answer"));
        UserCredentialModel input = new UserCredentialModel();
        input.setType(SecretQuestionCredentialProvider.SECRET_QUESTION);
        input.setValue(answer);
        context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), input);
        context.success();
    }

    @Override
    public void close() {

    }
}