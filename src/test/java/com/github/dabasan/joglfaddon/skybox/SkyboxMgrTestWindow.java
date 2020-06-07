package com.github.dabasan.joglfaddon.skybox;

import static com.github.dabasan.basis.vector.VectorFunctions.*;

import com.github.dabasan.joglf.gl.front.CameraFront;
import com.github.dabasan.joglf.gl.model.Model3DFunctions;
import com.github.dabasan.joglf.gl.util.screen.Screen;
import com.github.dabasan.joglf.gl.window.JOGLFWindow;
import com.github.dabasan.joglfaddon.blendscreens.BlendScreens;

class SkyboxMgrTestWindow extends JOGLFWindow {
	private SkyboxMgr skybox_mgr;
	private int model_handle;
	private BlendScreens blend_screens;
	private Screen screen_scene;
	private Screen screen_factors;
	private Screen screen_apply_factors;
	private Screen screen_skybox;
	private Screen screen_result;

	@Override
	public void Init() {
		String cubemap_directory = "./Data/Texture/Cubemap/old_outdoor_theater/";
		skybox_mgr = new SkyboxMgr("./Data/Model/OBJ/Skybox/skybox.obj",
				cubemap_directory + "px.png", cubemap_directory + "py.png",
				cubemap_directory + "pz.png", cubemap_directory + "nx.png",
				cubemap_directory + "ny.png", cubemap_directory + "nz.png");

		model_handle = Model3DFunctions.LoadModel("./Data/Model/OBJ/Teapot/teapot.obj");
		Model3DFunctions.TranslateModel(model_handle, VGet(0.0f, -10.0f, 0.0f));

		blend_screens = new BlendScreens();
	}

	@Override
	public void Reshape(int x, int y, int width, int height) {
		screen_scene = new Screen(width, height);
		screen_factors = new Screen(width, height);
		screen_apply_factors = new Screen(width, height);
		screen_skybox = new Screen(width, height);
		screen_result = new Screen(width, height);
	}

	@Override
	public void Update() {
		CameraFront.SetCameraPositionAndTarget_UpVecY(VGet(35.0f, 35.0f, 35.0f),
				VGet(0.0f, 0.0f, 0.0f));
	}

	@Override
	public void Draw() {
		screen_scene.Enable();
		screen_scene.Clear();
		Model3DFunctions.DrawModel(model_handle);
		screen_scene.Disable();

		skybox_mgr.GetReflectionMappingFactors(model_handle, screen_factors);
		blend_screens.Mul(screen_scene, screen_factors, screen_apply_factors);
		skybox_mgr.DrawSkybox(screen_skybox);
		blend_screens.Overlay(screen_apply_factors, screen_skybox, screen_result);
		screen_result.Draw(0, 0, this.GetWidth(), this.GetHeight());
	}
}
