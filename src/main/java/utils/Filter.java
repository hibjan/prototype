package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Filter {
    // ENV (1=Movies) ->
    // Type (m / r) ->
    // Filter (Genre / Director) ->
    // Values (Action / 108)
    private HashMap<Integer, HashMap<String, HashMap<String, HashSet<String>>>> filters = new HashMap<>();
    private Stack<State> states_stack;
    private State prev;

    public Filter(boolean isEnv, int currentEnv, int currentEnt){
        this.filters = new HashMap<>();
        this.states_stack = new Stack<>();
        this.prev = new State(isEnv, currentEnv, currentEnt, this.filters);
    }

    public void saveState(boolean isEnv, int currentEnv, int currentEnt) {
        states_stack.push(this.prev);
        this.prev = new State(isEnv, currentEnv, currentEnt, this.filters);
    }

    public State prevState(){
        State prev_state = states_stack.pop();
        this.filters = prev_state.getFilters();
        this.prev = prev_state;
        return prev_state;
    }

    public HashMap<Integer, HashMap<String, HashMap<String, HashSet<String>>>> getCurrent(){
        return this.filters;
    }

    public void addMetadataFilter(Integer current_env, String element, String value){
        addFilter(current_env, element, value, "m");
    }

    public void removeMetadataFilter(Integer current_env, String element, String value){
        removeFilter(current_env, element, value, "m");
    }

    public void addReferenceFilter(Integer current_env, String element, String value){
        addFilter(current_env, element, value, "r");
    }

    public void removeReferenceFilter(Integer current_env, String element, String value){
        removeFilter(current_env, element, value, "r");
    }

    private void addFilter(Integer current_env, String element, String value, String type){
        if(!filters.containsKey(current_env)){
            filters.put(current_env, new HashMap<>());
        }
        if(!filters.get(current_env).containsKey(type)){
            filters.get(current_env).put(type, new HashMap<>());
        }
        if(!filters.get(current_env).get(type).containsKey(element)){
            filters.get(current_env).get(type).put(element, new HashSet<>());
        }
        filters.get(current_env).get(type).get(element).add(value);
    }

    private void removeFilter(Integer current_env, String element, String value, String type){
        if(filters.containsKey(current_env) &&
        filters.get(current_env).containsKey(type) &&
        filters.get(current_env).get(type).containsKey(element)){
            filters.get(current_env).get(type).get(element).remove(value);
            if(filters.get(current_env).get(type).get(element).isEmpty()){
                filters.get(current_env).get(type).remove(element);
                if(filters.get(current_env).get(type).isEmpty()){
                    filters.get(current_env).remove(type);
                    if(filters.get(current_env).isEmpty()){
                        filters.remove(current_env);
                    }
                }
            }
        }
    }
}
