package org.ds.ss1.infimia;

import com.amazonaws.services.route53.infima.Lattice;
import com.amazonaws.services.route53.infima.SimpleSignatureShuffleSharder;
import com.amazonaws.services.route53.infima.SingleCellLattice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sample2 {
    public static void main(String... args) throws Exception {
        String[] endpoints = new String[] { "A", "B", "C", "D", "E","F","G","H", "I", "J", "K", "L" };

        SingleCellLattice<String> lattice = new SingleCellLattice<String>();
        lattice.addEndpoints(Arrays.asList(endpoints));

        SimpleSignatureShuffleSharder<String> sharder = new SimpleSignatureShuffleSharder<String>(5353L);

        Map<String, Integer> countByLetter = new HashMap<String, Integer>();
        for(int i = 0; i < 100000; i++) {
            UUID uuid = UUID.randomUUID();
            //System.out.println(sharder.shuffleShard(lattice, uuid.toString().getBytes(), 2));
            Lattice<String> shard = sharder.shuffleShard(lattice, uuid.toString().getBytes(), 2);

            for (String letter : shard.getAllEndpoints()) {
                if (countByLetter.containsKey(letter)) {
                    countByLetter.put(letter, countByLetter.get(letter) + 1);
                } else {
                    countByLetter.put(letter, 0);
                }
            }
        }

        System.out.println(countByLetter);
    }
}
