package meshlib;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.List;
import meshlib.conversion.DebugMeshBuilder;
import meshlib.conversion.MeshConverter;
import meshlib.data.BMeshProperty;
import meshlib.data.property.ColorProperty;
import meshlib.data.property.Vec3Property;
import meshlib.operator.EdgeOps;
import meshlib.operator.ExtrudeFace;
import meshlib.operator.FaceOps;
import meshlib.structure.BMesh;
import meshlib.structure.Edge;
import meshlib.structure.Face;
import meshlib.structure.Loop;
import meshlib.structure.Vertex;
import meshlib.util.BMeshVisualization;


public class Main extends SimpleApplication {
    @Override
    public void simpleInitApp() {
        //Mesh in = new Torus(128, 128, 2.0f, 4.0f);
        Mesh in = new Torus(32, 24, 1.2f, 2.5f);
        //Mesh in = new Sphere(32, 32, 5.0f);
        //Mesh in = new Box(1, 1, 1);

        BMesh bmesh = MeshConverter.convertGridMapped(in);
        processMesh(bmesh);
        bmesh.compactData();
        rootNode.attachChild(createDebugMesh(bmesh));
        //rootNode.attachChild(createMesh(bmesh));

        rootNode.addLight(new AmbientLight(ColorRGBA.White.mult(0.7f)));
        rootNode.addLight(new DirectionalLight(new Vector3f(-0.7f, -1, -0.9f).normalizeLocal(), ColorRGBA.White));

        flyCam.setMoveSpeed(10);
        cam.setFrustumPerspective(60, (float)cam.getWidth()/cam.getHeight(), 0.01f, 100f);
        viewPort.setBackgroundColor(hsb(0.75f, 0.2f, 0.15f));
    }


    private void processMesh(BMesh bmesh) {
        Vec3Property<Vertex> propPosition = Vec3Property.get(BMeshProperty.Vertex.POSITION, bmesh.vertices());
        EdgeOps edgeOps = new EdgeOps(bmesh);
        
        List<Edge> edges = new ArrayList<>();
        /*for(Edge e : bmesh.edges())
            edges.add(e);

        for(Edge e : edges) {
            Vector3f center = edgeOps.calcCenter(e);
            Vertex vert = bmesh.splitEdge(e);
            propPosition.set(vert, center);
        }*/

        /*edges.clear();
        for(Edge e : bmesh.edges())
            edges.add(e);

        for(Edge e : edges) {
            Vector3f center = edgeOps.calcCenter(e);
            Vertex vert = bmesh.splitEdge(e);
            propPosition.set(vert, center);
        }*/

        /*for(Face f : bmesh.faces()) {
            bmesh.invertFace(f);
            //bmesh.invertFace(f);
        }*/

        FaceOps faceOps = new FaceOps(bmesh);
        List<Face> faces = new ArrayList<>();
        for(Face f : bmesh.faces())
            faces.add(f);

        ExtrudeFace extr = new ExtrudeFace(bmesh);
        for(Face f : faces) {
            extr.extrude(f);
            extr.copyVertexProperties();
            
            Vector3f centroid = faceOps.calcCentroid(f);
            Vector3f normal = faceOps.calcNormal(f).multLocal(0.2f);

            for(Loop loop : f.loops()) {
                Vector3f p = propPosition.get(loop.vertex);
                p.subtractLocal(centroid);
                p.multLocal(0.7f);
                p.addLocal(centroid);
                p.addLocal(normal);
                propPosition.set(loop.vertex, p);
            }

            for(Face fRes : extr.getResultFaces()) {
                edges.clear();
                for(Loop l : fRes.loops())
                    edges.add(l.edge);

                for(Edge e : edges) {
                    Vector3f center = edgeOps.calcCenter(e);
                    Vertex vert  = bmesh.splitEdge(e);
                    propPosition.set(vert, center);
                }

                /*centroid = faceOps.calcCentroid(fRes);
                normal = faceOps.calcNormal(fRes).multLocal(0.1f);

                for(Loop loop : fRes.loops()) {
                    Vector3f p = propPosition.get(loop.vertex);
                    p.subtractLocal(centroid);
                    p.multLocal(0.95f);
                    p.addLocal(centroid);
                    p.addLocal(normal);
                    propPosition.set(loop.vertex, p);
                }*/
            }
        }
    }


    private Geometry createMesh(BMesh bmesh) {
        ColorProperty<Vertex> propVertexColor = new ColorProperty<>(BMeshProperty.Vertex.COLOR);
        bmesh.vertices().addProperty(propVertexColor);

        for(Vertex v : bmesh.vertices()) {
            ColorRGBA color = hsb((float)Math.random(), 0.8f, 0.8f);
            propVertexColor.set(v, color.r, color.g, color.b, color.a);
        }

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);

        Geometry geom = new Geometry("Geom", BMeshVisualization.create(bmesh));
        geom.setMaterial(mat);
        return geom;
    }

    
    private Node createDebugMesh(BMesh bmesh) {
        Node node = new Node("Debug");

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);
        //mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        DebugMeshBuilder debugMeshBuilder = new DebugMeshBuilder();
        debugMeshBuilder.apply(bmesh);
        Geometry geom = new Geometry("Geom", debugMeshBuilder.createMesh());
        geom.setMaterial(mat);
        node.attachChild(geom);

        Material matNormals = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matNormals.setBoolean("VertexColor", true);
        Geometry geomNormals = new Geometry("GeomNormals", DebugMeshBuilder.createNormals(bmesh, 0.33f));
        geomNormals.setMaterial(matNormals);
        node.attachChild(geomNormals);

        return node;
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
		int i = (int) hh;
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
