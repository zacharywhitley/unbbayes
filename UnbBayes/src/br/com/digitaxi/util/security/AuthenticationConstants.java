package br.com.digitaxi.util.security;

public final class AuthenticationConstants {

	public static final String USER_KEY = "_USER";
	public static final String USER_PROFILE = "_PROFILE";

	private AuthenticationConstants() {
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
