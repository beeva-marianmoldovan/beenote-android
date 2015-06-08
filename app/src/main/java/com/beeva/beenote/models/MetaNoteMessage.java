package com.beeva.beenote.models;


/**
 * Created by marianclaudiu on 5/06/15.
 */
public class MetaNoteMessage {

    public Note getNote() {
        return message;
    }

    public enum Operation {

        FETCH("GET"), REMOVE("DELETE"), UPDATE("PUT"), CREATE("POST");

        private String op;

        Operation(String op){
            this.op = op;
        }

        public String getOp() {
            return op;
        }
    }

    private String operation;
    private Note message;

    public MetaNoteMessage(Operation operation, Note message) {
        this.operation = operation.getOp();
        this.message = message;
    }

}
