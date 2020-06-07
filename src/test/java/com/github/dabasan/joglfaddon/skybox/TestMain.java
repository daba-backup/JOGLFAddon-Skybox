package com.github.dabasan.joglfaddon.skybox;

public class TestMain {
	public static void main(String[] args) {
		new TestMain();
	}
	public TestMain() {
		var window = new SkyboxMgrTestWindow();
		window.SetExitProcessWhenDestroyed();
	}
}
