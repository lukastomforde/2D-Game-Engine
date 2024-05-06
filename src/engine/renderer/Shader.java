package engine.renderer;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Shader {
    
    private int shaderProgramID;
    private boolean beingUsed = false;

    private String vertexSource;
    private String fragmentSource;
    private String filepath;

    public Shader(String filepath){
        this.filepath = filepath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] parts = source.split("(#type)( )+([a-zA-Z]+)");

            int eol = 0;
            for(int i=1; i<=2; ++i){
                int index = source.indexOf("#type", eol) + 6;
                eol = source.indexOf("\r\n", index);
                String pattern = source.substring(index, eol).trim();

                switch(pattern){
                    case "vertex":
                        vertexSource = parts[1];
                        break;
                    
                    case "fragment" :
                        fragmentSource = parts[2];
                        break;
    
                    default:
                        throw new IOException("Unexpected token '" + pattern + "'");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: Could not open file for shader; '" + filepath + "'!";
        }

//        System.out.println(vertexSource);
//        System.out.println(fragmentSource);
    }

    public void compile(){
        // ================================
        // Compile and link shaders
        // ================================

        int vertexID, fragmentID;

    	// First load and compile the vertex shader
        vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

        // Pass the shader source code to the gpu
        GL20.glShaderSource(vertexID, vertexSource);
        GL20.glCompileShader(vertexID);

        // check for errors in compilation
        int success = GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS);
        if (success == GL20.GL_FALSE) {
            int len = GL20.glGetShaderi(vertexID, GL20.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filepath + "'\n\tVerterx shader compilation failed.");
            System.out.println(GL20.glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // First load and compile the vertex shader
        fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        // Pass the shader source code to the gpu
        GL20.glShaderSource(fragmentID, fragmentSource);
        GL20.glCompileShader(fragmentID);

        // check for errors in compilation
        success = GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS);
        if (success == GL20.GL_FALSE) {
            int len = GL20.glGetShaderi(fragmentID, GL20.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filepath + "'\n\tFragment shader compilation failed.");
            System.out.println(GL20.glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // Link shaders and check for errors
        shaderProgramID = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgramID, vertexID);
        GL20.glAttachShader(shaderProgramID, fragmentID);
        GL20.glLinkProgram(shaderProgramID);

        // Check for linking errors
        success = GL20.glGetProgrami(shaderProgramID, GL20.GL_LINK_STATUS);
        if (success == GL20.GL_FALSE){
            int len = GL20.glGetShaderi(shaderProgramID, GL20.GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filepath + "' \n\tLinking of shaders failed.");
            System.out.println(GL20.glGetProgramInfoLog(shaderProgramID, len));
            assert false : "";
        }
    }

    public void use(){
        if(!beingUsed){
            // Bind shader Program
            GL20.glUseProgram(shaderProgramID);
            beingUsed = true;
        }
    }

    public void detach(){
        GL20.glUseProgram(0);
        beingUsed = false;
    }

    public void uploadMat4f(String varName, Matrix4f mat){
        int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
        use(); // make sure, we are using the shader
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        mat.get(matBuffer); // flatten the 2d array to a 1d array
        GL20.glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    public void uploadMat3f(String varName, Matrix3f mat){
        int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
        use(); // make sure, we are using the shader
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat.get(matBuffer); // flatten the 2d array to a 1d array
        GL20.glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    public void uploadMat2f(String varName, Matrix2f mat){
        int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
        use(); // make sure, we are using the shader
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(4);
        mat.get(matBuffer); // flatten the 2d array to a 1d array
        GL20.glUniformMatrix2fv(varLocation, false, matBuffer);
    }

    public void uploadVec4f(String varName, Vector4f vec){
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL20.glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
    }

    public void uploadVec3f(String varName, Vector3f vec){
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL20.glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    public void uploadVec2f(String varName, Vector2f vec){
        int varLocation = GL30.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL20.glUniform2f(varLocation, vec.x, vec.y);
    }
    
    public void uploadFloat(String varName, float val){
        int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL20.glUniform1f(varLocation, val);
    }

    public void uploadInt(String varName, int val){
        int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL20.glUniform1i(varLocation, val);
    }

    public void uploadTexture(String varName, int slot){
        int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL20.glUniform1i(varLocation, slot);
    }

    public void uploadTextureArray(String varName, int[] slots){
        int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
        use();
        GL20.glUniform1iv(varLocation, slots);
    }
}