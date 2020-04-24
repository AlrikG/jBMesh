package meshlib;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import meshlib.conversion.DebugMeshBuilder;
import meshlib.conversion.MeshConverter;
import meshlib.data.BMeshProperty;
import meshlib.data.property.ColorProperty;
import meshlib.structure.BMesh;
import meshlib.structure.Vertex;
import meshlib.util.BMeshVisualization;

public class Main extends SimpleApplication {
    private Mesh createMesh(Mesh in) {
        BMesh bmesh = MeshConverter.convert(in);
        bmesh.compactData();

        ColorProperty<Vertex> propVertexColor = new ColorProperty<>(BMeshProperty.Vertex.COLOR);
        bmesh.vertexData().addProperty(propVertexColor);

        float hue = 0;
        for(Vertex v : bmesh.vertices()) {
            ColorRGBA color = hsb(hue, 1.0f, 1.0f);
            hue += 0.71f;
            if(hue > 1.0f)
                hue -= 1.0f;
            propVertexColor.set(v, color.r, color.g, color.b, color.a);
        }

        return BMeshVisualization.create(bmesh);
    }

    
    private Mesh createDebugMesh(Mesh in) {
        BMesh bmesh = MeshConverter.convert(in);
        bmesh.compactData();

        DebugMeshBuilder debugMeshBuilder = new DebugMeshBuilder();
        debugMeshBuilder.apply(bmesh);
        return debugMeshBuilder.createMesh();
    }
    

    @Override
    public void simpleInitApp() {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);

        Geometry geom = new Geometry("Geom", createDebugMesh(new Box(1f, 1f, 1f)));
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        
        rootNode.addLight(new AmbientLight(ColorRGBA.White.mult(0.7f)));
        rootNode.addLight(new DirectionalLight(new Vector3f(-0.7f, -1, -0.9f).normalizeLocal(), ColorRGBA.White));

        flyCam.setMoveSpeed(10);
        cam.setFrustumPerspective(60, (float)cam.getWidth()/cam.getHeight(), 0.01f, 100f);
    }

    
    @Override
    public void simpleUpdate(float tpf) {}


    private ColorRGBA hsb(float h, float s, float b) {
        ColorRGBA color = new ColorRGBA();
        color.a = 1.0f;

		if (s == 0) {
			// achromatic ( grey )
			color.r = b;
			color.g = b;
			color.b = b;
			return color;
		}

		//float hh = h / 60.0f;
        float hh = h * 6f;
		int i = (int) Math.floor(hh);
		float f = hh - i;
		float p = b * (1 - s);
		float q = b * (1 - s * f);
		float t = b * (1 - s * (1 - f));

        switch(i) {
            case 0:
                color.r = b;
                color.g = t;
                color.b = p;
                break;
            case 1:
                color.r = q;
                color.g = b;
                color.b = p;
                break;
            case 2:
                color.r = p;
                color.g = b;
                color.b = t;
                break;
            case 3:
                color.r = p;
                color.g = q;
                color.b = b;
                break;
            case 4:
                color.r = t;
                color.g = p;
                color.b = b;
                break;
            default:
                color.r = b;
                color.g = p;
                color.b = q;
                break;
        }

        return color;
    }


    public static void main(String[] args) {
        /*PropertyAccessTest pa = new PropertyAccessTest(bmesh);
        pa.shouldWork();
        pa.shouldFailAtRuntime();*/

        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);

        Main app = new Main();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }
}
