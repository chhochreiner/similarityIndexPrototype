package org.openengsb.similarity.standard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.similarity.standard.impl.StandardIndexer;
import org.openengsb.similarity.standard.impl.StandardSearcher;

public class SimilarityTest {

    private StandardIndexer index;
    private StandardSearcher searcher;
    private static List<EDBObject> inserts;
    private static List<EDBObject> deletes;
    private static List<EDBObject> updates;

    @BeforeClass
    public static void generateLists() {
        inserts = buildEDBObjects(100, 30);
        deletes = inserts.subList(0, 10);
        updates = inserts.subList(20, 30);

        Integer counter = 20;

        for (EDBObject update : updates) {
            update.put(counter.toString(), counter);
        }
    }

    @Before
    public void setUp() throws IOException {
        index = new StandardIndexer();
        searcher = new StandardSearcher();
    }

    @After
    public void tearDown() {
        index.close();
        pruneIndex(new File(index.getPATH()));
    }

    @Test
    public void testIndexModificationInsert() throws IOException {
        assertEquals(0, index.getWriter().numDocs());
        index.updateIndex(inserts, null, null);
        assertEquals(100, index.getWriter().numDocs());
        index.close();
    }

    @Test
    public void testIndexModificationUpdate() throws IOException {
        assertEquals(0, index.getWriter().numDocs());
        index.updateIndex(inserts, null, null);
        assertEquals(100, index.getWriter().numDocs());
        index.updateIndex(null, updates, null);
        assertEquals(100, index.getWriter().numDocs());
        index.close();

        assertEquals(1, searcher.query(generateSearchString(updates.get(0))).size());
        assertEquals(updates.get(0).getOID(), searcher.query(generateSearchString(updates.get(0))).get(0));
    }

    @Test
    public void testIndexModificatioDelete() throws IOException {
        assertEquals(0, index.getWriter().numDocs());
        index.updateIndex(inserts, null, null);
        assertEquals(100, index.getWriter().numDocs());
        index.updateIndex(null, null, deletes);
        assertEquals(90, index.getWriter().numDocs());
        index.close();

        assertEquals(new ArrayList<String>(), searcher.query(generateSearchString(deletes.get(0))));
    }

    @Test
    public void findCollissionFails() {
        fail("Not yet implemented");
    }

    @Test
    public void findCollissionSuccess() {
        fail("Not yet implemented");
    }

    /**
     * generates an amount of random EDBObjects, based on a given number the size of the EDBObjects is defined by the
     * fieldCount
     * 
     */
    private static List<EDBObject> buildEDBObjects(int number, int fieldCount) {
        List<EDBObject> result = new ArrayList<EDBObject>();
        for (int i = 0; i < number; i++) {
            Map<String, Object> randomData = new HashMap<String, Object>();

            for (int j = 0; j < fieldCount; j++) {
                randomData.put("key" + String.valueOf(j), UUID.randomUUID().toString());
            }

            EDBObject e = new EDBObject(UUID.randomUUID().toString(), randomData);
            result.add(e);
        }
        return result;
    }

    private boolean pruneIndex(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    pruneIndex(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }

    private String generateSearchString(EDBObject sample) {
        String result = "";
        for (Map.Entry<String, Object> entry : sample.entrySet()) {
            if (result.length() != 0) {
                result += " AND ";
            }
            result += entry.getKey().toString() + ":" + entry.getValue().toString();
        }

        return result;
    }

}