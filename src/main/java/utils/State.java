package utils;

import java.util.HashMap;
import java.util.HashSet;

public class State {
    private boolean isEnv;
    private int current_env;
    private int current_ent;
    private HashMap<Integer, HashMap<String, HashMap<String, HashSet<String>>>> filters;

    public State(boolean isEnv, int current_env, int current_ent, HashMap<Integer, HashMap<String, HashMap<String, HashSet<String>>>> filters){
        this.isEnv = isEnv;
        this.current_env = current_env;
        this.current_ent = current_ent;

        this.filters = new HashMap<>();

        for(Integer env_filter : filters.keySet()) {
            this.filters.put(env_filter, new HashMap<>());
            for (String type : filters.get(env_filter).keySet()) {
                this.filters.get(env_filter).put(type, new HashMap<>());
                for (String filter : filters.get(env_filter).get(type).keySet()) {
                    this.filters.get(env_filter).get(type).put(filter, new HashSet<>(filters.get(env_filter).get(type).get(filter)));
                }
            }
        }
    }

    public boolean getIsEnv() {
        return this.isEnv;
    }

    public int getCurrentEnv() {
        return this.current_env;
    }

    public int getCurrentEnt() {
        return this.current_ent;
    }

    public HashMap<Integer, HashMap<String, HashMap<String, HashSet<String>>>> getFilters() {
        return this.filters;
    }
}
