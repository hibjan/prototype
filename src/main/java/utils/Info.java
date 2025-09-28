package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Info {
    // ENV (1=Movies) ->
    // Entity ID (1="X) ->
    // Field name ->
    // Content
    private HashMap<Integer, HashMap<Integer, HashMap<String, String>>> contents_map;
    // ENV (1=Movies) ->
    // Filter Name (Genres) ->
    // Filter Value (Action) ->
    // Set of IDs from ENV (Movies with Action Genre)
    private HashMap<Integer, HashMap<String, HashMap<String, HashSet<Integer>>>> metadata_map;
    // ENV (1=Movies) ->
    // Entity ID (1="X") ->
    // ENV Reference ID (2=People) ->
    // Reason ("Actor") ->
    // Set of IDs from ENV Reference (Actors reference by X)
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<String, HashSet<Integer>>>>> references_map;
    // ENV (1=Movies) ->
    // Column ->
    // Set of ID-Name
    private HashMap<Integer, HashMap<String, SortedSet<String>>> columns;
    // ENV (1=Movies) ->
    // Column ->
    // Type (m / r)
    private HashMap<Integer, HashMap<String, String>> column_type;
    // ENV (1=Movies) ->
    // Number of rows the table should have
    private HashMap<Integer, Integer> rows;
    // ENV (1=Movies) ->
    // Width of the columns the table should have
    private HashMap<Integer, Integer> column_width;
    // ENV (1=Movies) ->
    // Name of the environment
    private HashMap<Integer, String> envs;
    // ANSI escape codes
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";

    public Info(JSONObject dataset){
        this.contents_map = new HashMap<>();
        this.metadata_map = new HashMap<>();
        this.references_map = new HashMap<>();

        this.columns = new HashMap<>();
        this.column_type = new HashMap<>();
        this.rows = new HashMap<>();
        this.column_width = new HashMap<>();
        this.envs = new HashMap<>();

        this.processJSON(dataset);
    }

    private void processJSON(JSONObject dataset) {
        JSONArray collections = dataset.getJSONArray("collections");

        for(int i = 0; i < collections.length(); i++){
            JSONObject collection = (JSONObject) collections.get(i);

            contents_map.put((Integer) collection.get("id"), new HashMap<>());
            metadata_map.put((Integer) collection.get("id"), new HashMap<>());
            references_map.put((Integer) collection.get("id"), new HashMap<>());

            columns.put((Integer) collection.get("id"), new HashMap<>());
            column_type.put((Integer) collection.get("id"), new HashMap<>());
            rows.put((Integer) collection.get("id"), 0);
            column_width.put((Integer) collection.get("id"), 0);
            envs.put((Integer) collection.get("id"), (String) collection.get("name"));
        }

        JSONArray objects = dataset.getJSONArray("objects");

        for(int i = 0; i < objects.length(); i++){
            JSONObject object = (JSONObject) objects.get(i);

            //CONTENTS
            contents_map.get((Integer) object.get("collection_id")).put((Integer) object.get("id"), new HashMap<>());

            JSONObject contents = (JSONObject) object.get("contents");

            for (String key : contents.keySet()) {
                contents_map.get((Integer) object.get("collection_id")).get((Integer) object.get("id")).put(key, (String) contents.get(key));
            }

            //METADATA
            JSONObject metadata = (JSONObject) object.get("metadata");

            for (String key : metadata.keySet()) {
                if(!metadata_map.get((Integer) object.get("collection_id")).containsKey(key)) {
                    metadata_map.get((Integer) object.get("collection_id")).put(key, new HashMap<>());
                }

                //column
                if(!columns.get((Integer) object.get("collection_id")).containsKey(key)) {
                    columns.get((Integer) object.get("collection_id")).put(key, new TreeSet<>());
                    column_type.get((Integer) object.get("collection_id")).put(key, "m");
                    column_width.put((Integer) object.get("collection_id"), Math.max(column_width.get((Integer) object.get("collection_id")), key.length() + 2));
                }
                //column

                JSONArray metadata_values = (JSONArray) metadata.get(key);

                for(int j = 0; j < metadata_values.length(); j++){
                    if(!metadata_map.get((Integer) object.get("collection_id")).get(key).containsKey((String) metadata_values.get(j))) {
                        metadata_map.get((Integer) object.get("collection_id")).get(key).put((String) metadata_values.get(j), new HashSet<>());
                    }
                    metadata_map.get((Integer) object.get("collection_id")).get(key).get((String) metadata_values.get(j)).add((Integer) object.get("id"));

                    //column
                    int old_column_size = columns.get((Integer) object.get("collection_id")).get(key).size();
                    columns.get((Integer) object.get("collection_id")).get(key).add((String) metadata_values.get(j));
                    int new_column_size = columns.get((Integer) object.get("collection_id")).get(key).size();
                    if(new_column_size > old_column_size){
                        rows.put((Integer) object.get("collection_id"), Math.max(rows.get((Integer) object.get("collection_id")), new_column_size));
                        column_width.put((Integer) object.get("collection_id"), Math.max(column_width.get((Integer) object.get("collection_id")), metadata_values.get(j).toString().length() + 2));
                    }
                    //column
                }
            }

            //REFERENCES
            JSONArray references = (JSONArray) object.get("references");

            for(int j = 0; j < references.length(); j++) {
                JSONObject reference = (JSONObject) references.get(j);

                if (!references_map.get((Integer) object.get("collection_id")).containsKey((Integer) object.get("id"))) {
                    references_map.get((Integer) object.get("collection_id")).put((Integer) object.get("id"), new HashMap<>());
                }

                if (!references_map.get((Integer) object.get("collection_id")).get((Integer) object.get("id")).containsKey((Integer) reference.get("reference_collection_id"))) {
                    references_map.get((Integer) object.get("collection_id")).get((Integer) object.get("id")).put((Integer) reference.get("reference_collection_id"), new HashMap<>());
                }

                if (!references_map.get((Integer) object.get("collection_id")).get((Integer) object.get("id")).get((Integer) reference.get("reference_collection_id")).containsKey((String) reference.get("reason"))) {
                    references_map.get((Integer) object.get("collection_id")).get((Integer) object.get("id")).get((Integer) reference.get("reference_collection_id")).put((String) reference.get("reason"), new HashSet<>());
                }

                references_map.get((Integer) object.get("collection_id")).get((Integer) object.get("id")).get((Integer) reference.get("reference_collection_id")).get((String) reference.get("reason")).add((Integer) reference.get("reference_id"));

                //column
                if(!columns.get((Integer) object.get("collection_id")).containsKey((String) reference.get("reason"))) {
                    columns.get((Integer) object.get("collection_id")).put((String) reference.get("reason"), new TreeSet<>());
                    column_type.get((Integer) object.get("collection_id")).put((String) reference.get("reason"), "r");
                    column_width.put((Integer) object.get("collection_id"), Math.max(column_width.get((Integer) object.get("collection_id")), reference.get("reason").toString().length() + 2));
                }
                String reference_name = reference.get("name").toString() + " (" + reference.get("reference_id").toString() + ")";
                int old_column_size = columns.get((Integer) object.get("collection_id")).get((String) reference.get("reason")).size();
                columns.get((Integer) object.get("collection_id")).get((String) reference.get("reason")).add(reference_name);
                int new_column_size = columns.get((Integer) object.get("collection_id")).get((String) reference.get("reason")).size();
                if(new_column_size > old_column_size){
                    rows.put((Integer) object.get("collection_id"), Math.max(rows.get((Integer) object.get("collection_id")), new_column_size));
                    column_width.put((Integer) object.get("collection_id"), Math.max(column_width.get((Integer) object.get("collection_id")), reference_name.length() + 2));
                }
                //column
            }
        }
    }

    public void printColumns(int env, HashMap<Integer, HashMap<String, HashMap<String, HashSet<String>>>> filters){
        System.out.println();

        StringBuilder sb_env = new StringBuilder();
        sb_env.append("[");
        boolean first = true;
        for(Integer environ : this.envs.keySet()){
            String name = this.envs.get(environ);
            sb_env.append(first ? " " : ", ").append(environ == env ? BOLD + UNDERLINE + name + " (" + environ + ")" + RESET : name);
            if(first){
                first = false;
            }
        }
        sb_env.append(" ]");
        System.out.println(sb_env);

        int colWidth = this.column_width.get(env);
        int rows = this.rows.get(env);

        StringBuilder sb = new StringBuilder();
        StringBuilder sb_line = new StringBuilder();
        for(String column : this.columns.get(env).keySet()){
            sb.append(String.format("| %-" + colWidth + "s ", column));
            sb_line.append("+").append("-".repeat(colWidth + 2));
        }
        sb.append(" |");
        sb_line.append("+");
        String header = sb.toString();
        String line = sb_line.toString();

        System.out.println(line);
        System.out.println(header);
        System.out.println(line);


        for (int i = 0; i < rows; i++) {
            StringBuilder sb_row = new StringBuilder();
            for(String column : this.columns.get(env).keySet()){
                int j = 0;
                String col_name = "";
                for (String val : this.columns.get(env).get(column)) {
                    if (j == i) { // stop at third element
                        col_name = val;
                        break;
                    }
                    j++;
                }

                if(!col_name.isEmpty() && filters.containsKey(env) && column_type.get(env).containsKey(column)){
                    if(column_type.get(env).get(column).equals("m") && filters.get(env).containsKey("m") && filters.get(env).get("m").containsKey(column) && filters.get(env).get("m").get(column).contains(col_name)){
                        col_name =  "* " + col_name;
                    }
                    else if(column_type.get(env).get(column).equals("r")) {
                        int start = col_name.indexOf('(') + 1;
                        int end = col_name.indexOf(')');

                        String number = col_name.substring(start, end);

                        if(filters.get(env).containsKey("r") && filters.get(env).get("r").containsKey(column) && filters.get(env).get("r").get(column).contains(number)){
                            col_name =  "* " + col_name;
                        }
                    }
                }

                sb_row.append(String.format("| %-" + colWidth + "s ", col_name));
            }
            sb_row.append(" |");
            String row = sb_row.toString();
            System.out.println(row);
        }

        // Bottom line
        System.out.println(line);
        System.out.println();

    }

    public void printEntities(int env, HashMap<Integer, HashMap<String, HashMap<String, HashSet<String>>>> filters){
        HashSet<Integer> result = new HashSet<>();
        HashSet<Integer> selections = new HashSet<>();
        if(filters.isEmpty()){
            System.out.println("Every entity part of the collection: " + env);
        }
        else {
            for(Integer env_filter : filters.keySet()){
                for(String type : filters.get(env_filter).keySet()){
                    for(String filter : filters.get(env_filter).get(type).keySet()){
                        for(String value : filters.get(env_filter).get(type).get(filter)){
                            if(type.equals("m")){
                                if(env_filter.equals(env)){
                                    if(result.isEmpty()){
                                        result = metadata_map.get(env_filter).get(filter).get(value);
                                    }
                                    else {
                                        result.retainAll(metadata_map.get(env_filter).get(filter).get(value));
                                    }
                                }
                                else {
                                    HashSet<Integer> trans = new HashSet<>();
                                    for(Integer id : metadata_map.get(env_filter).get(filter).get(value)){
                                        for(String reason : references_map.get(env_filter).get(id).get(env).keySet()){
                                            if(trans.isEmpty()){
                                                trans = references_map.get(env_filter).get(id).get(env).get(reason);
                                            }
                                            else {
                                                trans.addAll(references_map.get(env_filter).get(id).get(env).get(reason));
                                            }
                                        }
                                    }
                                    if(result.isEmpty()){
                                        result = trans;
                                    }
                                    else {
                                        result.retainAll(trans);
                                    }
                                }
                            }
                            else if(type.equals("r")){
                                if(!env_filter.equals(env)){
                                    if(result.isEmpty()){
                                        result = references_map.get(env_filter).get(Integer.parseInt(value)).get(env).get(filter);
                                    }
                                    else {
                                        result.retainAll(references_map.get(env_filter).get(Integer.parseInt(value)).get(env).get(filter));
                                    }
                                }
                                else {
                                    selections.add(Integer.parseInt(value));
                                }
                            }
                        }
                    }
                }
            }
        }

        if(!selections.isEmpty() && !result.isEmpty()){
            result.retainAll(selections);
        }
        else if(!selections.isEmpty()){
            result = selections;
        }

        for(Integer id : result){
            System.out.println(contents_map.get(env).get(id).get("name") + " (" + id + ")");
        }
    }

    public void printEntity(int currentEnv, int id) {
        for(String content : contents_map.get(currentEnv).get(id).keySet()){
            String value = contents_map.get(currentEnv).get(id).get(content);
            System.out.println(UNDERLINE + content + RESET + ": " + value);
            System.out.println();
        }

        for(Integer reference_env : references_map.get(currentEnv).get(id).keySet()){
            System.out.println(BOLD + UNDERLINE + envs.get(reference_env) + " (" + reference_env + ")" + RESET + ":");
            for(String reason : references_map.get(currentEnv).get(id).get(reference_env).keySet()){
                System.out.println(reason + ":");
                for(Integer reference_id : references_map.get(currentEnv).get(id).get(reference_env).get(reason)){
                    System.out.println("    - " + contents_map.get(reference_env).get(reference_id).get("name") + " (" + reference_id + ")");
                }
            }
        }
    }
}