//package se.sundsvall.teamssender.test;
//
//import com.azure.identity.AuthorizationCodeCredential;
//import com.microsoft.graph.serviceclient.GraphServiceClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class GraphClientFactory {
//
//	private final GraphCredentialStorage storage;
//
//	@Autowired
//	public GraphClientFactory(GraphCredentialStorage storage) {
//		this.storage = storage;
//	}
//
//	public GraphServiceClient createClient() {
//		System.out.println("före create authcodecredential");
//		AuthorizationCodeCredential credential = storage.getCredential();
//		System.out.println("efter create authcodecredential");
//		if (credential == null)
//			throw new IllegalStateException("Ingen inloggning");
//
//		return new GraphServiceClient(credential);
//	}
//}
