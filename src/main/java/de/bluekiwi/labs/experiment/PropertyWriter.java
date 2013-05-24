package de.bluekiwi.labs.experiment;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import com.unister.semweb.topicmodeling.io.xml.CorpusXmlReader;
import com.unister.semweb.topicmodeling.utils.corpus.Corpus;
import com.unister.semweb.topicmodeling.utils.doc.Document;

import de.bluekiwi.labs.algorithm.NEDAlgo_HITS;
import de.bluekiwi.labs.util.DBpediaOwlReader;
import de.bluekiwi.labs.vis.MyNode;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class PropertyWriter {
    private static Logger log = LoggerFactory.getLogger(PropertyWriter.class);

    public static void main(String[] args) {
        String OUTPUT_FILE = "/data/r.usbeck/Dropbox/reuters.xml";
        String ONTOLOGY_FILE = "/data/r.usbeck/Dropbox/ontology_properties.txt";

        // String OUTPUT_FILE = "/Users/ricardousbeck/Dropbox/reuters.xml";
        // String ONTOLOGY_FILE = "/Users/ricardousbeck/Dropbox/ontology_properties.txt";

        CorpusXmlReader reader = new CorpusXmlReader(new File(OUTPUT_FILE));
        Corpus corpus = reader.getCorpus();

        // get all properties
        DBpediaOwlReader owl = new DBpediaOwlReader(ONTOLOGY_FILE);

        HashSet<String> propertiesToTest = new HashSet<String>();
        propertiesToTest.addAll(owl.hashset);

        NEDAlgo_HITS algo = new NEDAlgo_HITS(corpus.getNumberOfDocuments());
        double threshholdTrigram = 0.835;
        HashMap<String, Integer> hist = new HashMap<String, Integer>();
        for (Document document : corpus) {
            algo.runPreStep(document, threshholdTrigram, document.getDocumentId());
        }
        for (DirectedSparseGraph<MyNode, String> g : algo.getAllGraphs()) {
            for (String edge : g.getEdges()) {
                edge = edge.split(";")[1];
                if (hist.containsKey(edge)) {
                    hist.put(edge, hist.get(edge) + 1);
                } else {
                    hist.put(edge, 1);
                }
            }
        }

        Ordering<String> naturalReverseValueOrdering = Ordering.natural().nullsLast()
                .onResultOf(Functions.forMap(hist, null)).compound(Ordering.natural());
        ImmutableSortedMap<String, Integer> tmp = ImmutableSortedMap.copyOf(hist, naturalReverseValueOrdering);

        for (String edge : tmp.keySet())
            log.error(edge + " -> " + hist.get(edge));
    }
}