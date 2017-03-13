# keycloakext project
##Goals
The main goal of this keycloak authenticator project is to deliver the skeleton of an authenticator providing the following functionality :
1. Allow to be executed only according to some context values (for exemple, IP Range, etc...) - same concept than OptionalOTP
2. Leverage existing authenticators by allowing to delegate second authentication factor to an existing authenticator.
3. This is only a skeleton, nothing else right now - just to validate some assertions.
## Current Status
1. "Conditions" to enable or not 2FA is always true..
2. Available delegate authenticator list is hard coded.
3. Right now, only one real 2fa authenticator, reusing the OTP Authenticaor. the "empty" one is just to pass if otp not properly configured for current user
## How to test
1. Deploy the new authenticator (you can deploy it from your favorite IDE using EJB)
2. Copy secondfactor-authenticator-selector.ftl in themes/base/login directory
3. Create a Realm with a custom Brower flow. In the Form subflow, add it after the login form and set it as mandatory
4. Try it using the "user accout" application for your new realm.
## Missing and open to any good idea
1. Configuration : I was unable to display a multi value list (select available authenticator) - sounds like it's not part of the ftl template for config
2. Unable to create a configuration for an authenticator valid for the realm. (Imagine you'd like an SMS authenticator, it's not possible to create a configuration valid for the whole realm).
3. Is it a good idea to implement it like that ? (for exemple, using the Note in order to keep the selected authenticator for the second factor)
4. The "select" is really hugly, how to improve.
5. It could be great to improve authenticator configuration by adding the configuration of the selected 2fa authenticators in the same page (kind of "dynamic")
6. Feel free to send me comments regarding this code, how to improve it.

