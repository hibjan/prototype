package main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONObject;
import utils.Filter;
import utils.Info;


// Commands of navigation:
/*

add_mfilter tag=value       -> adds metadata filter (Genre=Action)

rm_mfilter tag=value        -> removes metadata filter (Genre=Action)

add_rfilter env->tag=value  -> adds reference filter (2->Director=108)

rm_rfilter env->tag=value   -> removes reference filter (2->Director=108)

select ent=value            -> shows entity details (ent=122)

follow env->id              -> follow entity details (1->122)

select env=value            -> show environment entities (env=1, env=this)

goback                      -> restores last environment or entity

exit                        -> stops program

 */

public class Main {

    public static void main(String[] args) {
        int current_env = 1;
        int current_ent = -1;
        boolean env = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        Info info = new Info(loadJSON());
        Filter filter = new Filter();

        while (true) {

            info.printColumns(current_env, filter.getCurrent());
            if(env){
                info.printEntities(current_env, filter.getCurrent());
            }
            else{
                info.printEntity(current_env, current_ent);
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

                    case "goback":
                        System.out.println("Restoring previous environment...");
                        break;

                    default:
                        System.out.println("Invalid command. Try again.");
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
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            return new JSONObject(content);
        }
        catch (IOException e){
            System.out.println("Error reading dataset: " + e.getMessage());
        }
        return null;
    }
}
