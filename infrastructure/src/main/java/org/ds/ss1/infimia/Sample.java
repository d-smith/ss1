package org.ds.ss1.infimia;

import com.amazonaws.services.route53.infima.Lattice;
import com.amazonaws.services.route53.infima.SingleCellLattice;
import com.amazonaws.services.route53.infima.StatefulSearchingShuffleSharder;
import com.amazonaws.services.route53.infima.StatefulSearchingShuffleSharder.FragmentStore;
import com.amazonaws.services.route53.infima.StatefulSearchingShuffleSharder.NoShardsAvailableException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Sample {
    private static class MockFragmentStore implements FragmentStore<String> {
        private final HashSet<String> store = new HashSet<String>();

        @Override
        public void saveFragment(List<String> fragment) {
            Collections.sort(fragment);
            store.add(fragment.toString());
        }

        @Override
        public boolean isFragmentUsed(List<String> fragment) {
            Collections.sort(fragment);
            return store.contains(fragment.toString());
        }
    }

    public static void main(String... args) throws Exception {
        String[] endpoints = new String[] { "A", "B", "C", "D", "E","F","G","H" };
        SingleCellLattice<String> lattice = new SingleCellLattice<String>();
        lattice.addEndpoints(Arrays.asList(endpoints));

        MockFragmentStore mockStore = new MockFragmentStore();
        StatefulSearchingShuffleSharder<String> sharder = new StatefulSearchingShuffleSharder<String>(mockStore);
        for(int i = 0; i < 28; i++) {
            Lattice<String> l = sharder.shuffleShard(lattice,2,1);
            System.out.println(l.toString());
        }

    }
}
