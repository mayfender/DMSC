package com.may.ple.phone.jainsip;

import javax.sip.ClientTransaction;

import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;

public class AccountManagerImpl implements AccountManager {


    @Override
	public UserCredentials getCredentials(ClientTransaction challengedTransaction, String realm) {
       return new UserCredentialsImpl("101","192.168.43.100","abc123");
    }

}
