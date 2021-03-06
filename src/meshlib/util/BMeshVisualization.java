package meshlib.util;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import meshlib.data.BMeshProperty;
import meshlib.data.property.ColorProperty;
import meshlib.data.property.Vec3Property;
import meshlib.operator.FaceOps;
import meshlib.structure.BMesh;
import meshlib.structure.Face;
import meshlib.structure.Loop;
import meshlib.structure.Vertex;

public class BMeshVisualization {
    public static Mesh create(BMesh bmesh) {
        bmesh.vertices().compactData();

        FaceOps faceOps = new FaceOps(bmesh);
        Vector3f normal = new Vector3f();
        Vec3Property<Vertex> propNormals = new Vec3Property<>("Normal");
        bmesh.vertices().addProperty(propNormals);
        // Need to split vertices for each face/loop

        ArrayList<Integer> indices = new ArrayList<>(bmesh.faces().size() * 3);
        ArrayList<Loop> loops = new ArrayList<>(4);

        for(Face f : bmesh.faces()) {
            for(Loop loop : f.loops()) {
                loops.add(loop);
                faceOps.calcNormal(f, normal);
                /*nbuf[in++] = normal.x;
                nbuf[in++] = normal.y;
                nbuf[in++] = normal.z;*/
            }

            // Fan-like triangulation for inner area
            // TODO: This creates superfluous triangles for collinear edges?
            for(int i=2; i<loops.size(); ++i) {
                indices.add(loops.get(0).vertex.getIndex());
                indices.add(loops.get(i-1).vertex.getIndex());
                indices.add(loops.get(i).vertex.getIndex());
            }

            loops.clear();
        }

        int[] ibuf = new int[indices.size()];
        for(int i=0; i<indices.size(); ++i)
            ibuf[i] = indices.get(i);

        Vec3Property<Vertex> propPosition = Vec3Property.get(BMeshProperty.Vertex.POSITION, bmesh.vertices());
        ColorProperty<Vertex> propVertexColor = ColorProperty.get(BMeshProperty.Vertex.COLOR, bmesh.vertices());

        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.Triangles);
        
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(propPosition.array()));
        //mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(nbuf));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(propVertexColor.array()));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(ibuf));
        mesh.updateBound();

        return mesh;
    }
}
