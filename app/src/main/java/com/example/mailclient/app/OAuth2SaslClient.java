package com.example.mailclient.app;

import org.apache.harmony.javax.security.auth.callback.Callback;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.apache.harmony.javax.security.auth.callback.NameCallback;
import org.apache.harmony.javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.harmony.javax.security.sasl.SaslClient;
import org.apache.harmony.javax.security.sasl.SaslException;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Created by teo on 06/06/14.
 */


/**
 * An OAuth2 implementation of SaslClient.
 */
class OAuth2SaslClient implements SaslClient {
    private static final Logger logger =
            Logger.getLogger(OAuth2SaslClient.class.getName());

    private final String oauthToken;
    private final CallbackHandler callbackHandler;

    private boolean isComplete = false;

    /**
     * Creates a new instance of the OAuth2SaslClient. This will ordinarily only
     * be called from OAuth2SaslClientFactory.
     */
    public OAuth2SaslClient(String oauthToken,
                            CallbackHandler callbackHandler) {
        this.oauthToken = oauthToken;
        this.callbackHandler = callbackHandler;
    }

    public String getMechanismName() {
        return "XOAUTH2";
    }

    public boolean hasInitialResponse() {
        return true;
    }

    public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
        if (isComplete) {
            // Empty final response from server, just ignore it.
            return new byte[] { };
        }

        NameCallback nameCallback = new NameCallback("Enter name");
        Callback[] callbacks = new Callback[] { nameCallback };
        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            throw new SaslException("Unsupported callback: " + e);
        } catch (IOException e) {
            throw new SaslException("Failed to execute callback: " + e);
        }
        String email = nameCallback.getName();

        byte[] response = String.format("user=%s\1auth=Bearer %s\1\1", email,
                oauthToken).getBytes();
        isComplete = true;
        return response;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public byte[] unwrap(byte[] incoming, int offset, int len)
            throws SaslException {
        throw new IllegalStateException();
    }

    public byte[] wrap(byte[] outgoing, int offset, int len)
            throws SaslException {
        throw new IllegalStateException();
    }

    public Object getNegotiatedProperty(String propName) {
        if (!isComplete()) {
            throw new IllegalStateException();
        }
        return null;
    }

    public void dispose() throws SaslException {
    }
}
