package com.fintrack.ucic_tracker.service;

import java.util.HashMap;
import java.util.Map;


public class UnionFind {

 
    private final Map<Long, Long> parent = new HashMap<>();

  
    private final Map<Long, Integer> rank = new HashMap<>();

   
    public void add(Long id) {
        if (!parent.containsKey(id)) {
            parent.put(id, id);  
            rank.put(id, 0);
        }
    }

   
    public Long find(Long x) {
        if (!parent.get(x).equals(x)) {
            parent.put(x, find(parent.get(x))); // path compression
        }
        return parent.get(x);
    }

    
    public void union(Long x, Long y) {
        Long rootX = find(x);
        Long rootY = find(y);

        if (rootX.equals(rootY)) return; 

      
        if (rank.get(rootX) < rank.get(rootY)) {
            parent.put(rootX, rootY);
        } else if (rank.get(rootX) > rank.get(rootY)) {
            parent.put(rootY, rootX);
        } else {
            parent.put(rootY, rootX);
            rank.put(rootX, rank.get(rootX) + 1);
        }
    }

    
    public boolean connected(Long x, Long y) {
        return find(x).equals(find(y));
    }

   
    public Map<Long, Long> getParentMap() {
        return parent;
    }
}
