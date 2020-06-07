package com.github.dabasan.joglfaddon.skybox;

import static com.github.dabasan.basis.vector.VectorFunctions.*;
import static com.github.dabasan.joglf.gl.wrapper.GLWrapper.*;
import static com.jogamp.opengl.GL.*;

import com.github.dabasan.joglf.gl.front.CameraFront;
import com.github.dabasan.joglf.gl.model.Model3DFunctions;
import com.github.dabasan.joglf.gl.shader.ShaderProgram;
import com.github.dabasan.joglf.gl.util.screen.Screen;
import com.github.dabasan.joglf.gl.window.JOGLFWindow;

class SkyboxMgrTestWindow extends JOGLFWindow {
	private SkyboxMgr skybox_mgr;
	private int model_handle;
	private Screen screen_scene;
	private Screen screen_factors;
	private Screen screen_skybox;

	@Override
	public void Init() {
		String cubemap_directory = "./Data/Texture/Cubemap/old_outdoor_theater/";
		skybox_mgr = new SkyboxMgr("./Data/Model/OBJ/Skybox/skybox.obj",
				cubemap_directory + "px.png", cubemap_directory + "py.png",
				cubemap_directory + "pz.png", cubemap_directory + "nx.png",
				cubemap_directory + "ny.png", cubemap_directory + "nz.png");

		model_handle = Model3DFunctions.LoadModel("./Data/Model/OBJ/Teapot/teapot.obj");
		Model3DFunctions.RemoveAllPrograms(model_handle);
		Model3DFunctions.AddProgram(model_handle, new ShaderProgram("simple_3d"));
		Model3DFunctions.TranslateModel(model_handle, VGet(0.0f, -10.0f, 0.0f));

		glDisable(GL_CULL_FACE);
	}

	@Override
	public void Reshape(int x, int y, int width, int height) {
		screen_scene = new Screen(width, height);
		screen_factors = new Screen(width, height);
		screen_skybox = new Screen(width, height);
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
		skybox_mgr.DrawSkybox(screen_skybox);
		screen_factors.Draw(0, 0, this.GetWidth(), this.GetHeight());
		// screen_skybox.Draw(0, 0, this.GetWidth(), this.GetHeight());
	}
}
