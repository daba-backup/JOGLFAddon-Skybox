package com.github.dabasan.joglfaddon.skybox;

import static com.github.dabasan.joglf.gl.wrapper.GLWrapper.*;
import static com.jogamp.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.github.dabasan.joglf.gl.front.CameraFront;
import com.github.dabasan.joglf.gl.model.FlipVOption;
import com.github.dabasan.joglf.gl.model.Model3DFunctions;
import com.github.dabasan.joglf.gl.shader.ShaderProgram;
import com.github.dabasan.joglf.gl.texture.TextureMgr;
import com.github.dabasan.joglf.gl.util.screen.ScreenBase;
import com.jogamp.common.nio.Buffers;

/**
 * Skybox
 * 
 * @author Daba
 *
 */
public class SkyboxMgr {
	private int[] texture_handles;
	private int cubemap_id;
	private int skybox_handle;

	private ShaderProgram simple_3d;
	private ShaderProgram reflection_mapping;
	private ShaderProgram refraction_mapping;

	public SkyboxMgr(String skybox_filename, String px, String py, String pz, String nx, String ny,
			String nz) {
		// Load textures for cubemap.
		texture_handles = new int[6];
		texture_handles[0] = TextureMgr.LoadTexture(px);
		texture_handles[1] = TextureMgr.LoadTexture(py);
		texture_handles[2] = TextureMgr.LoadTexture(pz);
		texture_handles[3] = TextureMgr.LoadTexture(nx);
		texture_handles[4] = TextureMgr.LoadTexture(ny);
		texture_handles[5] = TextureMgr.LoadTexture(nz);

		// Generate a texture for cubemap.
		int[] targets = new int[]{GL_TEXTURE_CUBE_MAP_POSITIVE_X, GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
				GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
				GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z};

		ByteBuffer[] data_buffers = new ByteBuffer[6];
		for (int i = 0; i < 6; i++) {
			data_buffers[i] = TextureMgr.GetTextureImage(texture_handles[i]);
		}

		IntBuffer texture_ids = Buffers.newDirectIntBuffer(1);
		glGenTextures(1, texture_ids);
		cubemap_id = texture_ids.get(0);

		final int TEXTURE_WIDTH = TextureMgr.GetTextureWidth(texture_handles[0]);
		final int TEXTURE_HEIGHT = TextureMgr.GetTextureHeight(texture_handles[0]);
		glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap_id);
		for (int i = 0; i < 6; i++) {
			glTexImage2D(targets[i], 0, GL_RGBA, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, GL_RGBA,
					GL_UNSIGNED_BYTE, data_buffers[i]);
		}

		glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

		// Skybox is drawn with simple_3d (one of the default programs).
		simple_3d = new ShaderProgram("simple_3d");
		skybox_handle = Model3DFunctions.LoadModel(skybox_filename, FlipVOption.ALL);
		Model3DFunctions.RemoveAllPrograms(skybox_handle);
		Model3DFunctions.AddProgram(skybox_handle, simple_3d);

		// Apply textures to the skybox model.
		for (int i = 0; i < 6; i++) {
			Model3DFunctions.ChangeModelTexture(skybox_handle, i, texture_handles[i]);
		}

		int[] skybox_texture_handles = Model3DFunctions.GetModelTextureHandles(skybox_handle);
		for (int skybox_texture_handle : skybox_texture_handles) {
			// JOGLFramework sets GL_LINEAR by default (as of v11.7.0),
			// which causes visible lines on the edges of the skybox.
			// Change the parameters to GL_NEAREST in order to improve drawing
			// results.
			TextureMgr.BindTexture(skybox_texture_handle);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		}

		reflection_mapping = new ShaderProgram("dabasan/skybox/reflection_mapping",
				"./Data/Shader/330/addon/dabasan/skybox/reflection_mapping/vshader.glsl",
				"./Data/Shader/330/addon/dabasan/skybox/reflection_mapping/fshader.glsl");
		refraction_mapping = new ShaderProgram("dabasan/skybox/refraction_mapping",
				"./Data/Shader/330/addon/dabasan/skybox/refraction_mapping/vshader.glsl",
				"./Data/Shader/330/addon/dabasan/skybox/refraction_mapping/fshader.glsl");
	}

	public void Dispose() {
		for (int i = 0; i < 6; i++) {
			TextureMgr.DeleteTexture(texture_handles[i]);
		}
		Model3DFunctions.DeleteModel(skybox_handle);

		IntBuffer texture_ids = Buffers.newDirectIntBuffer(new int[]{cubemap_id});
		glDeleteTextures(texture_ids.capacity(), texture_ids);
	}

	public void SetCubemap(ShaderProgram program, String sampler_name, int texture_unit) {
		program.Enable();
		glActiveTexture(GL_TEXTURE0 + texture_unit);
		glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap_id);
		program.SetUniform(sampler_name, texture_unit);
	}

	public void GetReflectionMappingFactors(int model_handle, ScreenBase dst) {
		reflection_mapping.Enable();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap_id);
		reflection_mapping.SetUniform("cubemap", 0);
		Model3DFunctions.TransferModel(model_handle);
		reflection_mapping.Disable();
	}
	public void GetRefractionMappingFactors(int model_handle, ScreenBase dst) {
		refraction_mapping.Enable();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap_id);
		refraction_mapping.SetUniform("cubemap", 0);
		Model3DFunctions.TransferModel(model_handle);
		refraction_mapping.Disable();
	}

	public void DrawSkybox() {
		float near = CameraFront.GetCameraNear();
		float far = CameraFront.GetCameraFar();

		// Update zNear and zFar of the camera to draw a huge skybox.
		CameraFront.SetCameraNearFar(100.0f, 5000.0f);
		// Transfer the values to the "simple_3d" program.
		CameraFront.Update(simple_3d);
		// Draw the skybox.
		glDisable(GL_DEPTH_TEST);
		Model3DFunctions.DrawModel(skybox_handle);
		glEnable(GL_DEPTH_TEST);

		// Restore the original zNear and zFar.
		CameraFront.SetCameraNearFar(near, far);
	}
}
