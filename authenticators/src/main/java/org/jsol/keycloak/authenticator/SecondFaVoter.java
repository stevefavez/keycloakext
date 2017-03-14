package org.jsol.keycloak.authenticator;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;

import java.util.function.Predicate;

/**
 * Created by Steve Favez on 14.03.2017.
 */
public class SecondFaVoter {
    public static boolean voteFor2FARequired(AuthenticationFlowContext authenticationFlowContext, KeycloakSession keycloakSession) {
        Tuple<AuthenticationFlowContext, KeycloakSession> paramTuple = new Tuple<>(authenticationFlowContext, keycloakSession) ;
        return remoteIpRequires2FA().test(paramTuple) ;
    }

    static Predicate<Tuple> remoteIpRequires2FA() {
        return tuple -> true ;
    }

    /** stupid inner class - hope they'll add it in jdk 9*/
    public static class Tuple<F, S> {
        F first ;
        S second ;

        public Tuple(F aFirst, S aSecond) {
            this.first = aFirst ;
            this.second = aSecond ;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }
}
