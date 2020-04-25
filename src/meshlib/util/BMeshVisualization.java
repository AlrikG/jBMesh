package meshlib.util;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.List;
import meshlib.data.BMeshProperty;
import meshlib.data.property.ColorProperty;
import meshlib.data.property.Vec3Property;
import meshlib.structure.BMesh;
import meshlib.structure.Face;
import meshlib.structure.Loop;
import meshlib.structure.Vertex;

public class BMeshVisualization {
    public static Mesh create(BMesh bmesh) {
        bmesh.vertexData().compact();
        
        List<Face> faces = bmesh.faces();
        int[] indices = new int[faces.size() * 3];
        int index = 0;
        for(int i=0; i<faces.size(); ++i) {
            Face f = faces.get(i);

            int numVertices = 0;
            for(Loop loop : f.loops()) {
                numVertices++;
                indices[index++] = loop.vertex.getIndex();
            }

            if(numVertices != 3) {
                throw new RuntimeException("Face is not a triangle");
            }
        }

        Vec3Property<Vertex> propPosition = Vec3Property.get(BMeshProperty.Vertex.POSITION, bmesh.vertexData());
        ColorProperty<Vertex> propVertexColor = ColorProperty.get(BMeshProperty.Vertex.COLOR, bmesh.vertexData());

        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.Triangles);
        
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(propPosition.array()));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(propVertexColor.array()));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
        mesh.updateBound();

        return mesh;
    }
}
