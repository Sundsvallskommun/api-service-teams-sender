//package se.sundsvall.teamssender.test;
//
//import com.azure.identity.AuthorizationCodeCredential;
//import org.springframework.stereotype.Component;
//
//@Component
//public class GraphCredentialStorage {
//	private AuthorizationCodeCredential credential;
//
//	public synchronized void setCredential(AuthorizationCodeCredential credential) {
//		this.credential = credential;
//	}
//
//	public synchronized AuthorizationCodeCredential getCredential() {
//		return credential;
//	}
//
//	public boolean isAuthenticated() {
//		return credential != null;
//	}
//}
