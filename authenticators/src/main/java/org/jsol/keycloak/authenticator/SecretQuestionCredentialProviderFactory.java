package org.jsol.keycloak.authenticator;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SecretQuestionCredentialProviderFactory implements CredentialProviderFactory<SecretQuestionCredentialProvider> {
    @Override
    public String getId() {
        return "secret-question";
    }

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new SecretQuestionCredentialProvider(session);
    }
}