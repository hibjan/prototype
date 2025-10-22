package main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONObject;
import utils.Filter;
import utils.Info;
import utils.State;


// Commands of navigation:
/*

add_mfilter tag=value       -> adds metadata filter (Genre=Action)

rm_mfilter tag=value        -> removes metadata filter (Genre=Action)

add_rfilter env->tag=value  -> adds reference filter (2->Director=108)

rm_rfilter env->tag=value   -> removes reference filter (2->Director=108)

add_xfilter env=value       -> adds reference filter (2=Director)

rm_xfilter env=value   -> removes reference filter (2=Director)

select ent=value            -> shows entity details (ent=122)

follow env->id              -> follow entity details (1->122)

select env=value            -> show environment entities (env=1, env=this)

restore                     -> restores last state

goback                      -> goes to last environment viewed

exit                        -> stops program

 */

public class Main {

    public static void main(String[] args) {
        int current_env = 1;
        int current_ent = -1;
        boolean env = true;

        boolean error = false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        Info info = new Info(loadJSON());
        Filter filter = new Filter(env, current_env, current_ent);

        while (true) {

            if(error) {
                error = false;
            }
            else{
                if (env) {
                    info.printColumns(current_env, filter.getCurrent());
                    info.printEntities(current_env, filter.getCurrent());
                } else {
                    info.printEntity(current_env, current_ent);
                }
            }

            System.out.print("\nEnter command: ");
            String input;
            try {
                input = reader.readLine();
                if (input == null) break; // end of stream (Ctrl+D)

                input = input.trim();

                // Split command into keyword + args
                String[] parts = input.split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String argument = (parts.length > 1) ? parts[1] : "";
                String[] argument_parts;
                String element;
                String value;
                String type;
                int int_value;
                boolean back = false;

                switch (command) {
                    case "exit":
                        return;

                    case "select":
                        argument_parts = argument.split("=", 2);
                        type = argument_parts[0];
                        int_value = !argument_parts[1].equals("this") ? Integer.parseInt(argument_parts[1]) : -1;
                        switch(type){
                            case "env":
                                env = true;
                                if(!argument_parts[1].equals("this")){
                                    current_env = int_value;
                                }
                                filter.newEnv(current_env);
                                break;
                            case "ent":
                                env = false;
                                current_ent = int_value;
                                break;
                            default:
                                System.out.println("Error: invalid argument");
                                break;
                        }
                        break;

                    case "follow":
                        argument_parts = argument.split("->", 2);
                        int new_env = Integer.parseInt(argument_parts[0]);
                        int id = Integer.parseInt(argument_parts[1]);
                        current_env = new_env;
                        current_ent = id;
                        filter.newEnv(current_env);
                        break;

                    case "add_mfilter":
                        argument_parts = argument.split("=", 2);
                        element = argument_parts[0];
                        value = argument_parts[1];
                        filter.addMetadataFilter(current_env, element, value);
                        break;

                    case "rm_mfilter":
                        argument_parts = argument.split("=", 2);
                        element = argument_parts[0];
                        value = argument_parts[1];
                        filter.removeMetadataFilter(current_env, element, value);
                        break;

                    case "add_rfilter":
                        argument_parts = argument.split("->", 2);
                        int_value = Integer.parseInt(argument_parts[0]);
                        argument_parts = argument_parts[1].split("=", 2);
                        element = argument_parts[0];
                        value = argument_parts[1];
                        filter.addReferenceFilter(int_value, element, value);
                        break;

                    case "rm_rfilter":
                        argument_parts = argument.split("->", 2);
                        int_value = Integer.parseInt(argument_parts[0]);
                        argument_parts = argument_parts[1].split("=", 2);
                        element = argument_parts[0];
                        value = argument_parts[1];
                        filter.removeReferenceFilter(int_value, element, value);
                        break;


                    case "add_xfilter":
                        argument_parts = argument.split("=", 2);
                        int_value = Integer.parseInt(argument_parts[0]);
                        value = argument_parts[1];
                        filter.addReasonFilter(int_value, value, String.valueOf(current_env));
                        current_env = int_value;
                        filter.newEnv(current_env);
                        break;

                    case "rm_xfilter":
                        argument_parts = argument.split("=", 2);
                        int_value = Integer.parseInt(argument_parts[0]);
                        value = argument_parts[1];
                        filter.removeReasonFilter(int_value, value, String.valueOf(current_env));
                        break;

                    case "restore":
                        back = true;
                        State prev_state = filter.prevState();
                        env = prev_state.getIsEnv();
                        current_env = prev_state.getCurrentEnv();
                        current_ent = prev_state.getCurrentEnt();
                        filter.newEnv(current_env);
                        break;

                    case "goback":


                        break;

                    default:
                        error = true;
                        System.out.println("\nInvalid command. Try again.");
                }

                if(!back && !error){
                    filter.saveState(env, current_env, current_ent);
                }
            }
            catch (IOException e) {
                System.out.println("Error reading input: " + e.getMessage());
            }
            catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }

    private static JSONObject loadJSON(){
        try {
            String filePath = "src/main/resources/films_dataset.json";
            //String filePath = "src/main/resources/test.json";
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            return new JSONObject(content);
        }
        catch (IOException e){
            System.out.println("Error reading dataset: " + e.getMessage());
        }
        return null;
    }
}
