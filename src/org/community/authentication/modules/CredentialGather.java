/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */

package org.community.authentication.modules;

import java.security.Principal;
import java.util.Map;
import java.util.ResourceBundle;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.spi.InvalidPasswordException;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;



public class CredentialGather extends AMLoginModule
{

    // Name for the debug-log
    private final static String DEBUG_NAME = "CredentialGather";

    // Name of the resource bundle
    private final static String amAuthCredentialGather = "amAuthCredentialGather";

    // User names for authentication logic
    private final static String USERNAME = "demo";
    private final static String ERROR_1_NAME = "test1";
    private final static String ERROR_2_NAME = "test2";

    // Orders defined in the callbacks file
    private final static int STATE_BEGIN = 1;
    private final static int STATE_AUTH = 2;
    private final static int STATE_ERROR = 3;

    private final static Debug debug = Debug.getInstance(DEBUG_NAME);

    private Map options;
    private ResourceBundle bundle;



    public CredentialGather()
    {
        super();
    }



    @Override
    // This method stores service attributes and localized properties
    // for later use.
    public void init(Subject subject, Map sharedState, Map options)
    {
        if (debug.messageEnabled())
        {
            debug.message("CredentialGather::init");
        }
        this.options = options;
        bundle = amCache.getResBundle(amAuthCredentialGather, getLoginLocale());
    }



    @Override
    public int process(Callback[] callbacks, int state) throws LoginException
    {

        if (debug.messageEnabled())
        {
            debug.message("CredentialGather::process state: " + state);
        }

        switch (state)
        {

            case STATE_BEGIN:
                // No time wasted here - simply modify the UI and
                // proceed to next state
                substituteUIStrings();
                return STATE_AUTH;

            case STATE_AUTH:
                // Get data from callbacks. Refer to callbacks XML file.
                NameCallback nc = (NameCallback) callbacks[0];
                PasswordCallback pc = (PasswordCallback) callbacks[1];
                String username = nc.getName();
                String password = new String(pc.getPassword());

                // First errorstring is stored in "credentialgather-error-1" property.
                if (username.equals(ERROR_1_NAME))
                {
                    setErrorText("credentialgather-error-1");
                    return STATE_ERROR;
                }

                // Second errorstring is stored in "credentialgather-error-2" property.
                if (username.equals(ERROR_2_NAME))
                {
                    setErrorText("credentialgather-error-2");
                    return STATE_ERROR;
                }

                if (username.equals(USERNAME) && password.equals("changeit"))
                {
                    return ISAuthConstants.LOGIN_CHALLENGE;
                }

                throw new InvalidPasswordException("password is wrong", USERNAME);

            case STATE_ERROR:
                return STATE_ERROR;
            default:
                throw new AuthLoginException("invalid state");

        }
    }



    @Override
    public Principal getPrincipal()
    {
        return new CredentialGatherPrincipal(USERNAME);
    }



    private void setErrorText(String err) throws AuthLoginException
    {
        // Receive correct string from properties and substitute the
        // header in callbacks order 3.
        substituteHeader(STATE_ERROR, bundle.getString(err));
    }



    private void substituteUIStrings() throws AuthLoginException
    {
        // Get service specific attribute configured in OpenAM
        //String ssa = CollectionHelper.getMapAttr(options,
        //        "credentialgather-service-specific-attribute");

        // Get property from bundle
        String new_hdr = bundle.getString("credentialgather-ui-login-header");
        substituteHeader(STATE_AUTH, new_hdr);

        Callback[] cbs_phone = getCallback(STATE_AUTH);

        replaceCallback(STATE_AUTH, 0, new NameCallback(bundle
                .getString("credentialgather-ui-username-prompt")));

        replaceCallback(STATE_AUTH, 1, new PasswordCallback(bundle
                .getString("credentialgather-ui-password-prompt"), false));
    }

}